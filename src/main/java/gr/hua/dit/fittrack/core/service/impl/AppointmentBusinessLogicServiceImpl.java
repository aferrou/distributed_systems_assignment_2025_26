package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.*;
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

    private static final int MAX_ACTIVE_APPOINTMENTS = 5;

    // Each appointment has a duration of 1 hour
    private static final long APPOINTMENT_DURATION_HOURS = 1;

    private final AppointmentMapper appointmentMapper;
    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WeatherPort weatherPort;

    public AppointmentBusinessLogicServiceImpl(final AppointmentMapper appointmentMapper,
                                               final AppointmentRepository appointmentRepository,
                                               final PersonRepository personRepository,
                                               final CurrentUserProvider currentUserProvider,
                                               final WeatherPort weatherPort) {
        if (appointmentMapper == null) throw new NullPointerException();
        if (appointmentRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (weatherPort == null) throw new NullPointerException();

        this.appointmentMapper = appointmentMapper;
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.weatherPort = weatherPort;
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

        // 1. Max active appointments per user (maximum 5)
        final long activeCount = this.appointmentRepository.countByUserIdAndStatusIn(userId, ACTIVE);

        if (activeCount >= MAX_ACTIVE_APPOINTMENTS) {
            throw new IllegalStateException("Έχετε φτάσει το μέγιστο όριο των " + MAX_ACTIVE_APPOINTMENTS + " ενεργών ραντεβού.");
        }

        // 2. Appointment date cannot be in the past
        if (request.scheduledAt() != null && request.scheduledAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }

        // 3. Prevent overlapping appointments for the user
        // Each appointment has a duration of 1 hour
        if (request.scheduledAt() != null) {
            final Instant newAppointmentStart = request.scheduledAt();
            final Instant newAppointmentEnd = newAppointmentStart.plusSeconds(APPOINTMENT_DURATION_HOURS * 3600);

            // Find all active appointments for this user that have a scheduled time
            final List<Appointment> userActiveAppointments =
                    this.appointmentRepository.findByUserIdAndStatusInAndScheduledAtIsNotNull(userId, ACTIVE);

            // Check if any existing appointment overlaps with the new one
            for (Appointment existingAppointment : userActiveAppointments) {
                final Instant existingStart = existingAppointment.getScheduledAt();
                final Instant existingEnd = existingStart.plusSeconds(APPOINTMENT_DURATION_HOURS * 3600);

                // Two appointments overlap if:
                // newStart < existingEnd AND newEnd > existingStart
                if (newAppointmentStart.isBefore(existingEnd) && newAppointmentEnd.isAfter(existingStart)) {
                    throw new IllegalStateException("Έχετε ήδη ραντεβού σε αυτή την ώρα. Κάθε ραντεβού διαρκεί 1 ώρα και δεν μπορείτε να έχετε επικαλυπτόμενα ραντεβού.");
                }
            }
        }

        // 4. Prevent overlapping appointments for the trainer
        // Each appointment has a duration of 1 hour
        if (request.scheduledAt() != null) {
            final Instant newAppointmentStart = request.scheduledAt();
            final Instant newAppointmentEnd = newAppointmentStart.plusSeconds(APPOINTMENT_DURATION_HOURS * 3600);

            // Find all active appointments for this trainer that have a scheduled time
            final List<Appointment> trainerActiveAppointments =
                    this.appointmentRepository.findByTrainerIdAndStatusInAndScheduledAtIsNotNull(trainerId, ACTIVE);

            // Check if any existing appointment overlaps with the new one
            for (Appointment existingAppointment : trainerActiveAppointments) {
                final Instant existingStart = existingAppointment.getScheduledAt();
                final Instant existingEnd = existingStart.plusSeconds(APPOINTMENT_DURATION_HOURS * 3600);

                // Two appointments overlap if:
                // newStart < existingEnd AND newEnd > existingStart
                if (newAppointmentStart.isBefore(existingEnd) && newAppointmentEnd.isAfter(existingStart)) {
                    throw new IllegalStateException("Ο προπονητής έχει ήδη ραντεβού σε αυτή την ώρα. Κάθε ραντεβού διαρκεί 1 ώρα. Παρακαλώ επιλέξτε άλλη ώρα.");
                }
            }
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

        return appointmentView;
    }

    @Transactional
    @Override
    public AppointmentView cancelAppointment(final Long appointmentId) {
        if (appointmentId == null) throw new NullPointerException("Appointment id cannot be null");
        if (appointmentId <= 0) throw new IllegalArgumentException("Appointment id must be positive");

        // --------------------------------------------------
        final Appointment appointment = this.appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment does not exist"));

        // Security - both user and trainer can cancel
        // --------------------------------------------------
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        final long userId = appointment.getUser().getId();
        final long trainerId = appointment.getTrainer().getId();

        if (currentUser.id() != userId && currentUser.id() != trainerId) {
            throw new SecurityException("Only the user or trainer of this appointment can cancel it");
        }

        // Rules
        // --------------------------------------------------
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed appointment");
        }

        // --------------------------------------------------
        appointment.setStatus(AppointmentStatus.CANCELLED);

        final Appointment savedAppointment = this.appointmentRepository.save(appointment);
        final AppointmentView appointmentView = this.appointmentMapper.convertAppointmentToAppointmentView(savedAppointment);

        return appointmentView;
    }
}
