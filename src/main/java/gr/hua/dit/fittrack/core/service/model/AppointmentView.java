package gr.hua.dit.fittrack.core.service.model;

import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.model.TrainingType;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;

import java.time.Instant;

/**
 * General view of {@link gr.hua.dit.fittrack.core.model.Appointment} entity.
 *
 * @see gr.hua.dit.fittrack.core.model.Appointment
 * @see AppointmentBusinessLogicService
 */
public record AppointmentView(
        long id,
        PersonView user,
        PersonView trainer,
        AppointmentStatus status,
        TrainingType trainingType,
        String userNotes,
        String trainerNotes,
        Instant requestedAt,
        Instant confirmedAt,
        Instant inProgressAt,
        Instant completedAt
) {}
