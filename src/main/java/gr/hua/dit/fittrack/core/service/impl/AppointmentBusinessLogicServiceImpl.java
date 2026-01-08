package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.*;
import gr.hua.dit.fittrack.core.port.SmsNotificationPort;
import gr.hua.dit.fittrack.core.port.WeatherPort;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;

import gr.hua.dit.fittrack.core.service.mapper.AppointmentMapper;
import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.ConfirmAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import jakarta.persistence.EntityNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link AppointmentBusinessLogicService}.
 * TODO some parts can be reused (e.g., security checks)
 */
@Service
public class AppointmentBusinessLogicServiceImpl implements AppointmentBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentBusinessLogicServiceImpl.class);

    private static final Set<AppointmentStatus> ACTIVE = Set.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED);

    private static final int MAX_ACTIVE_APPOINTMENTS = 3;

    private final AppointmentMapper appointmentMapper;
    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsNotificationPort smsNotificationPort;

    private final WeatherPort weatherPort;

    public AppointmentBusinessLogicServiceImpl(final AppointmentMapper appointmentMapper,
                                               final AppointmentRepository appointmentRepository,
                                               final PersonRepository personRepository,
                                               final CurrentUserProvider currentUserProvider,
                                               final SmsNotificationPort smsNotificationPort,
                                               final WeatherPort weatherPort) {
        if (appointmentMapper == null) throw new NullPointerException();
        if (appointmentRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        if (weatherPort == null) throw new NullPointerException();

        this.appointmentMapper = appointmentMapper;
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsNotificationPort = smsNotificationPort;
        this.weatherPort = weatherPort;
    }

    private void notifyPerson(final AppointmentView appointmentView, final PersonType recipientType) {
        String phoneNumber;

        switch (recipientType) {
            case TRAINER -> phoneNumber = appointmentView.trainer().mobilePhoneNumber();
            case USER -> phoneNumber = appointmentView.user().mobilePhoneNumber();
            default -> throw new IllegalArgumentException("Unsupported PersonType: " + recipientType);
        }

        String message = String.format(
                "Appointment %d status updated to: %s",
                appointmentView.id(),
                appointmentView.status().name()
        );

        boolean sent = smsNotificationPort.sendSms(phoneNumber, message);
        if (!sent) {
            LOGGER.warn("Failed to send SMS to {}", phoneNumber);
        }
    }

    @Override
    public Optional<AppointmentView> getAppointment(final Long id) {
        if (id == null) throw new NullPointerException("Appointment id cannot be null");
        if (id <= 0) throw new IllegalArgumentException("Appointment id must be positive");

        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();

        // --------------------------------------------------

        final Appointment appointment;
        try {
            appointment = this.appointmentRepository.getReferenceById(id);
        } catch (EntityNotFoundException ignored) {
            return Optional.empty();
        }

        // --------------------------------------------------

        // Access control
        // Current user must be the USER or TRAINER of this appointment
        final long ownerId = switch (currentUser.type()) {
            case TRAINER -> appointment.getTrainer().getId();
            case USER -> appointment.getUser().getId();
            default -> throw new SecurityException("Unsupported PersonType: " + currentUser.type());
        };

        if (currentUser.id() != ownerId) {
            return Optional.empty(); // Appointment not accessible by this user
        }

        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(appointment);

        return Optional.of(appointmentView);
    }

    @Override
    public List<AppointmentView> getAppointments() {
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        final List<Appointment> appointmentList;

        switch (currentUser.type()) {
            case TRAINER -> appointmentList = this.appointmentRepository.findAllByTrainerId(currentUser.id());
            case USER -> appointmentList = this.appointmentRepository.findAllByUserId(currentUser.id());
            default -> throw new SecurityException("Unsupported PersonType: " + currentUser.type());
        }

        return appointmentList.stream()
                .map(this.appointmentMapper::convertAppointmentToAppointmentView)
                .toList();
    }

    @Transactional
    @Override
    public AppointmentView requestAppointment(@Valid final CreateAppointmentRequest request, final boolean notify) {
        if (request == null) throw new NullPointerException();

        // --------------------------------------------------
        final long userId = request.userId();
        final long trainerId = request.trainerId();
        final TrainingType trainingType = request.trainingType();
        final String userNotes = request.userNotes();

        // --------------------------------------------------

        final Person user = this.personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final Person trainer = this.personRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        if (user.getType() != PersonType.USER) {
            throw new IllegalArgumentException("userId must refer to a USER");
        }
        if (trainer.getType() != PersonType.TRAINER) {
            throw new IllegalArgumentException("trainerId must refer to a TRAINER");
        }

        // --------------------------------------------------

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.USER) {
            throw new SecurityException("USER type required");
        }
        if (currentUser.id() != userId) {
            throw new SecurityException("Authenticated user does not match the appointment's userId");
        }

        // Business rules
        // --------------------------------------------------

        // 1. Max 1 active appointment with the same trainer
        if (this.appointmentRepository.existsByUserIdAndTrainerIdAndStatusIn(userId, trainerId, ACTIVE)) {
            throw new IllegalStateException("User already has an active appointment with this trainer");
        }

        // 2. Max active appointments per user
        final long activeCount = this.appointmentRepository.countByUserIdAndStatusIn(userId, ACTIVE);

        if (activeCount >= MAX_ACTIVE_APPOINTMENTS) {
            throw new IllegalStateException("Maximum active appointments reached");
        }

        // 3. Appointment date cannot be in the past
        if (request.scheduledAt() != null && request.scheduledAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }

        // 4. Prevent overlapping appointments for the trainer
        if (request.scheduledAt() != null &&
                this.appointmentRepository.existsByTrainerIdAndScheduledAt(trainerId, request.scheduledAt())) {
            throw new IllegalStateException("Trainer already has an appointment at this time");
        }

        // Weather check for outdoor training
        // --------------------------------------------------
        if (trainingType == TrainingType.OUTDOOR_TRAINING &&
                request.latitude() != null &&
                request.longitude() != null &&
                request.scheduledAt() != null) {

            final LocalDate appointmentDate = LocalDate.ofInstant(
                    request.scheduledAt(),
                    ZoneId.systemDefault()
            );

            final WeatherForecast forecast = weatherPort.getForecast(
                    request.latitude(),
                    request.longitude(),
                    appointmentDate
            );

            if (!forecast.isSuitableForOutdoorTraining()) {
                LOGGER.warn("Weather may not be suitable for outdoor training on {}: {}",
                        appointmentDate, forecast);
                // You can either:
                // 1. Just warn (current approach)
                // 2. Throw exception to block appointment
                // 3. Add warning to user notes
            }
        }

        // --------------------------------------------------
        final Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTrainer(trainer);
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setTrainingType(trainingType);
        appointment.setUserNotes(userNotes);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setLatitude(request.latitude());
        appointment.setLongitude(request.longitude());
        appointment.setCreatedAt(Instant.now());

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        if (notify) {
            this.notifyPerson(appointmentView, PersonType.TRAINER);
        }

        return appointmentView;
    }


    @Transactional
    @Override
    public AppointmentView confirmAppointment(@Valid final ConfirmAppointmentRequest request) {
        if (request == null) throw new NullPointerException();

        // --------------------------------------------------
        final long appointmentId = request.id();

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
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new IllegalArgumentException("Only REQUESTED appointments can be confirmed");
        }

        // --------------------------------------------------
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(Instant.now());

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        // Notify the user
        this.notifyPerson(appointmentView, PersonType.USER);

        return appointmentView;
    }

    @Transactional
    @Override
    public AppointmentView completeAppointment(@Valid final CompleteAppointmentRequest request) {
        if (request == null) throw new NullPointerException();

        // --------------------------------------------------
        final long appointmentId = request.id();
        final String trainerNotes = request.trainerNotes();

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
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only CONFIRMED appointments can be completed");
        }

        // --------------------------------------------------

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setTrainerNotes(trainerNotes);
        appointment.setCompletedAt(Instant.now());

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        // Notify the user
        this.notifyPerson(appointmentView, PersonType.USER);

        return appointmentView;
    }
}
