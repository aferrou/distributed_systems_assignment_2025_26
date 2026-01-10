package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.*;
import gr.hua.dit.fittrack.core.port.SmsNotificationPort;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;

import gr.hua.dit.fittrack.core.service.mapper.AppointmentMapper;
import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.StartAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import jakarta.persistence.EntityNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link AppointmentBusinessLogicService}.
 *
 * <p>
 * TODO some parts can be reused (e.g., security checks)
 */
@Service
public class AppointmentBusinessLogicServiceImpl implements AppointmentBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentBusinessLogicServiceImpl.class);

    private static final Set<AppointmentStatus> ACTIVE = Set.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS);

    private static final int MAX_ACTIVE_APPOINTMENTS = 3;

    private final AppointmentMapper appointmentMapper;
    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsNotificationPort smsNotificationPort;

//    private final WeatherPort weatherPort;

    public AppointmentBusinessLogicServiceImpl(final AppointmentMapper appointmentMapper,
                                               final AppointmentRepository appointmentRepository,
                                               final PersonRepository personRepository,
                                               final CurrentUserProvider currentUserProvider,
                                               final SmsNotificationPort smsNotificationPort
//                                               final WeatherPort weatherPort
    ) {
        if (appointmentMapper == null) throw new NullPointerException();
        if (appointmentRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
//        if (weatherPort == null) throw new NullPointerException();

        this.appointmentMapper = appointmentMapper;
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsNotificationPort = smsNotificationPort;
//        this.weatherPort = weatherPort;
    }

    private void notifyPerson(final AppointmentView appointmentView, final PersonType type) {
        final String e164;

        if (type == PersonType.TRAINER) {
            e164 = appointmentView.trainer().mobilePhoneNumber();
        } else if (type == PersonType.USER) {
            e164 = appointmentView.user().mobilePhoneNumber();
        } else {
            throw new RuntimeException("Unreachable");
        }

        final String content = String.format("Appointment %s new status: %s", appointmentView.id(), appointmentView.status().name());
        final boolean sent = this.smsNotificationPort.sendSms(e164, content);

        if (!sent) {
            LOGGER.warn("SMS send to {} failed", e164);
        }
    }

    @Override
    public Optional<AppointmentView> getAppointment(final Long id) {
        if (id == null) throw new NullPointerException();
        if (id <= 0) throw new IllegalArgumentException();

        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();

        // --------------------------------------------------

        final Appointment appointment;
        try {
            appointment = this.appointmentRepository.getReferenceById(id);
        } catch (EntityNotFoundException ignored) {
            return Optional.empty();
        }

        // Current user must be the USER or TRAINER of this appointment
        // --------------------------------------------------

        final long appointmentPersonId;
        if (currentUser.type() == PersonType.TRAINER) {
            appointmentPersonId = appointment.getTrainer().getId();
        } else if (currentUser.type() == PersonType.USER) {
            appointmentPersonId = appointment.getUser().getId();
        } else {
            throw new SecurityException("unsupported PersonType");
        }
        if (currentUser.id() != appointmentPersonId) {
            return Optional.empty(); // this Appointment does not exist for this user.
        }

        // --------------------------------------------------

        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(appointment);

        // --------------------------------------------------

        return Optional.of(appointmentView);
    }

    @Override
    public List<AppointmentView> getAppointments() {
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        final List<Appointment> appointmentList;
        if (currentUser.type() == PersonType.TRAINER) {
            appointmentList = this.appointmentRepository.findAllByTrainerId(currentUser.id());
        } else if (currentUser.type() == PersonType.USER) {
            appointmentList = this.appointmentRepository.findAllByUserId(currentUser.id());
        } else {
            throw new SecurityException("unsupported PersonType");
        }
        return appointmentList.stream()
                .map(this.appointmentMapper::convertAppointmentToAppointmentView)
                .toList();
    }

    @Transactional
    @Override
    public AppointmentView createAppointment(@Valid final CreateAppointmentRequest createAppointmentRequest, final boolean notify) {
        if (createAppointmentRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long userId = createAppointmentRequest.userId();
        final long trainerId = createAppointmentRequest.trainerId();
        final TrainingType trainingType = createAppointmentRequest.trainingType();
        final String userNotes = createAppointmentRequest.userNotes();


        // --------------------------------------------------

        final Person user = this.personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final Person trainer = this.personRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        // --------------------------------------------------

        if (user.getType() != PersonType.USER) {
            throw new IllegalArgumentException("userId must refer to a USER");
        }
        if (trainer.getType() != PersonType.TRAINER) {
            throw new IllegalArgumentException("trainerId must refer to a TRAINER");
        }

        // Security
        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.USER) {
            throw new SecurityException("USER type required");
        }
        if (currentUser.id() != userId) {
            throw new SecurityException("Authenticated user does not match the appointment's userId");
        }

        // Rules
        // --------------------------------------------------

        // Rule 1: Max 1 active appointment with the same trainer
        if (this.appointmentRepository.existsByUserIdAndTrainerIdAndStatusIn(userId, trainerId, ACTIVE)) {
            throw new IllegalStateException("User already has an active appointment with this trainer");
        }

        // Rule 2: Max active appointments per user
        final long activeCount = this.appointmentRepository.countByUserIdAndStatusIn(userId, ACTIVE);

        if (activeCount >= MAX_ACTIVE_APPOINTMENTS) {
            throw new IllegalStateException("Maximum active appointments reached");
        }

        // Rule 3: Appointment date cannot be in the past
        if (createAppointmentRequest.scheduledAt() != null && createAppointmentRequest.scheduledAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }

        // Rule 4: Prevent overlapping appointments for the trainer
        if (createAppointmentRequest.scheduledAt() != null &&
                this.appointmentRepository.existsByTrainerIdAndScheduledAt(trainerId, createAppointmentRequest.scheduledAt())) {
            throw new IllegalStateException("Trainer already has an appointment at this time");
        }

        // Weather check for outdoor training
        // --------------------------------------------------
//        if (trainingType == TrainingType.OUTDOOR_TRAINING &&
//                createAppointmentRequest.latitude() != null &&
//                createAppointmentRequest.longitude() != null &&
//                createAppointmentRequest.scheduledAt() != null) {
//
//            final LocalDate appointmentDate = LocalDate.ofInstant(
//                    createAppointmentRequest.scheduledAt(),
//                    ZoneId.systemDefault()
//            );
//
//            final WeatherForecast forecast = weatherPort.getForecast(
//                    createAppointmentRequest.latitude(),
//                    createAppointmentRequest.longitude(),
//                    appointmentDate
//            );
//
//            if (!forecast.isSuitableForOutdoorTraining()) {
//                LOGGER.warn("Weather may not be suitable for outdoor training on {}: {}",
//                        appointmentDate, forecast);
//            }
//        }

        // --------------------------------------------------

        Appointment appointment = new Appointment();
        // appointment.setId(); // auto-generated
        appointment.setUser(user);
        appointment.setTrainer(trainer);
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setTrainingType(trainingType);
        appointment.setUserNotes(userNotes);
        appointment.setRequestedAt(Instant.now());
        appointment = this.appointmentRepository.save(appointment);

        // --------------------------------------------------

        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(appointment);

        // --------------------------------------------------

        if (notify) {
            this.notifyPerson(appointmentView, PersonType.TRAINER);
        }

        // --------------------------------------------------

        return appointmentView;
    }

    //    @Transactional
//    @Override
//    public AppointmentView confirmAppointment(@Valid final ConfirmAppointmentRequest confirmRequest) {
//        if (confirmRequest == null) throw new NullPointerException();
//
//        final long appointmentId = confirmRequest.id();
//        final String trainerNotes = confirmRequest.trainerNotes(); // optional confirmation notes
//
//        final Appointment appointment = this.appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Appointment does not exist"));
//
//        // Security - only the assigned trainer can confirm
//        final long trainerId = appointment.getTrainer().getId();
//        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
//        if (currentUser.type() != PersonType.TRAINER) {
//            throw new SecurityException("Trainer type/role required");
//        }
//        if (currentUser.id() != trainerId) {
//            throw new SecurityException("Authenticated trainer does not match the appointment's trainerId");
//        }
//
//        // Rules
//        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
//            throw new IllegalArgumentException("Only REQUESTED appointments can be confirmed");
//        }
//
//        // Optional: Check trainer availability at the scheduled time
//        if (appointment.getScheduledAt() != null) {
//            boolean hasConflict = this.appointmentRepository.existsByTrainerIdAndScheduledAtAndStatusIn(
//                    trainerId,
//                    appointment.getScheduledAt(),
//                    Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS)
//            );
//            if (hasConflict) {
//                throw new IllegalStateException("Trainer already has a confirmed appointment at this time");
//            }
//        }
//
//        // Update appointment
//        appointment.setStatus(AppointmentStatus.CONFIRMED);
//        appointment.setConfirmedAt(Instant.now());
//        appointment.setTrainerNotes(trainerNotes != null ? trainerNotes : appointment.getTrainerNotes());
//
//        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
//        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);
//
//        // Notify user about confirmation
//        this.notifyPerson(appointmentView, PersonType.USER);
//
//        return appointmentView;
//    }

//    @Transactional
//    @Override
//    public AppointmentView declineAppointment(@Valid final DeclineAppointmentRequest declineRequest) {
//        if (declineRequest == null) throw new NullPointerException();
//
//        final long appointmentId = declineRequest.id();
//        final String declineReason = declineRequest.declineReason();
//
//        final Appointment appointment = this.appointmentRepository.findById(appointmentId)
//                .orElseThrow(() -> new IllegalArgumentException("Appointment does not exist"));
//
//        // Security - only the assigned trainer can decline
//        final long trainerId = appointment.getTrainer().getId();
//        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
//        if (currentUser.type() != PersonType.TRAINER) {
//            throw new SecurityException("Trainer type/role required");
//        }
//        if (currentUser.id() != trainerId) {
//            throw new SecurityException("Authenticated trainer does not match the appointment's trainerId");
//        }
//
//        // Rules
//        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
//            throw new IllegalArgumentException("Only REQUESTED appointments can be declined");
//        }
//
//        // Update appointment
//        appointment.setStatus(AppointmentStatus.CANCELLED);
//        appointment.setCancelledAt(Instant.now());
//        appointment.setCancellationReason(declineReason);
//        appointment.setCancelledBy(PersonType.TRAINER);
//
//        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
//        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);
//
//        // Notify user about decline
//        this.notifyPerson(appointmentView, PersonType.USER);
//
//        return appointmentView;
//    }

    @Transactional
    @Override
    public AppointmentView startAppointment(@Valid final StartAppointmentRequest startAppointmentRequest) {
        if (startAppointmentRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long appointmentId = startAppointmentRequest.id();

        // --------------------------------------------------

        final Appointment appointment = this.appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment does not exist"));

        // Security.
        // --------------------------------------------------

        final long trainerId = appointment.getTrainer().getId();
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.TRAINER) {
            throw new SecurityException("Trainer type/role required");
        }
        if (currentUser.id() != trainerId) {
            throw new SecurityException("Authenticated trainer does not match the appointment's trainerId");
        }

        // Rules.
        // --------------------------------------------------

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) { //confirmed
            throw new IllegalArgumentException("Only confirmed appointments can be started");
        }

        // --------------------------------------------------

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setInProgressAt(Instant.now());

        // --------------------------------------------------

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);

        // --------------------------------------------------

        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        // --------------------------------------------------

        this.notifyPerson(appointmentView, PersonType.USER);

        // --------------------------------------------------

        return appointmentView;
    }

    @Transactional
    @Override
    public AppointmentView completeAppointment(@Valid final CompleteAppointmentRequest completeAppointmentRequest) {
        if (completeAppointmentRequest == null) throw new NullPointerException();

        // Unpack.
        // --------------------------------------------------

        final long appointmentId = completeAppointmentRequest.id();
        final String trainerNotes = completeAppointmentRequest.trainerNotes();

        // --------------------------------------------------

        final Appointment appointment = this.appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment does not exist"));

        // Security
        // --------------------------------------------------

        final long trainerId = appointment.getTrainer().getId();
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.TRAINER) {
            throw new SecurityException("Trainer type/role required");
        }
        if (currentUser.id() != trainerId) {
            throw new SecurityException("Authenticated trainer does not match the appointment's trainerId");
        }

        // Rules
        // --------------------------------------------------

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only IN_PROGRESS appointments can be completed");
        }

        // --------------------------------------------------

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setTrainerNotes(trainerNotes);
        appointment.setCompletedAt(Instant.now());

        // --------------------------------------------------

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);

        // --------------------------------------------------

        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        // --------------------------------------------------

        this.notifyPerson(appointmentView, PersonType.USER);

        // --------------------------------------------------

        return appointmentView;
    }
}
