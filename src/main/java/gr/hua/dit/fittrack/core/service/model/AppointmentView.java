package gr.hua.dit.fittrack.core.service.model;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.model.TrainingType;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;

import java.time.Instant;

/**
 * General view of {@link Appointment} DTO entity.
 *
 * @see Appointment
 * @see AppointmentBusinessLogicService
 */
public record AppointmentView(
        Long id,
        PersonView user,
        PersonView trainer,
        AppointmentStatus status,
        TrainingType trainingType,
        String userNotes,
        String trainerNotes,
        Instant scheduledAt,
        Instant createdAt,
        Instant confirmedAt,
        Instant completedAt,
        Double latitude,
        Double longitude,
        WeatherForecast weatherForecast
) {}