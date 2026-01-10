package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.port.SmsNotificationPort;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;

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

    private final AppointmentRepository appointmentRepository;
    private final SmsNotificationPort smsNotificationPort;

    public AppointmentReminderService(final AppointmentRepository appointmentRepository,
                                 final SmsNotificationPort smsNotificationPort) {
        if (appointmentRepository == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.appointmentRepository = appointmentRepository;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void remindTrainerOfStaleRequestedAppointments() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);
        final List<Appointment> appointmentList = this.appointmentRepository.findByStatusAndRequestedAtBefore(AppointmentStatus.REQUESTED, cutoff);
        for (final Appointment appointment : appointmentList) {
            final String e164 = appointment.getTrainer().getMobilePhoneNumber();
            final String content = String.format("Reminder: Appointment %s REQUESTED", appointment.getId());
            this.smsNotificationPort.sendSms(e164, content);
        }
    }
}
