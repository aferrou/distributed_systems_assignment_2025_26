package gr.hua.dit.fittrack.web.ui.model;

import gr.hua.dit.fittrack.core.model.TrainingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record CreateAppointmentForm(
        @NotNull @Positive Long trainerId,
        TrainingType trainingType,  // Optional - defaults to GENERAL_FITNESS
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
        @Size(max = 1000) String userNotes,
        // For outdoor training
        Double latitude,
        Double longitude
) {
    public TrainingType getTrainingTypeOrDefault() {
        return trainingType != null ? trainingType : TrainingType.GENERAL_FITNESS;
    }

    public Instant getScheduledAtAsInstant() {
        return scheduledAt != null ? scheduledAt.atZone(ZoneId.systemDefault()).toInstant() : null;
    }
}
