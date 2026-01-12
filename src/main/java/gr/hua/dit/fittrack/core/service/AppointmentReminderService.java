package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing appointment reminders.
 */
@Service
public class AppointmentReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentReminderService.class);

    private final AppointmentRepository appointmentRepository;

    public AppointmentReminderService(final AppointmentRepository appointmentRepository) {
        if (appointmentRepository == null) throw new NullPointerException();
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void remindTrainersOfStaleRequestedAppointments() {
        final Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);
        final List<Appointment> staleAppointments = this.appointmentRepository.findByStatusAndCreatedAtBefore(AppointmentStatus.REQUESTED, cutoff);
        for (final Appointment appointment : staleAppointments) {
            // Log reminder instead of sending SMS
            LOGGER.info("Reminder: Appointment {} is still awaiting confirmation from trainer {}",
                    appointment.getId(),
                    appointment.getTrainer().getEmailAddress());
        }
    }
}
