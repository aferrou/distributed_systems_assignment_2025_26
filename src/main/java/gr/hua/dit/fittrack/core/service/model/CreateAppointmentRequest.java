package gr.hua.dit.fittrack.core.service.model;

import gr.hua.dit.fittrack.core.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateAppointmentRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long trainerId,
        @NotNull TrainingType trainingType,
        @NotNull @NotBlank @Size(max = 1000) String userNotes,
        @NotNull Instant scheduledAt,
        Double latitude,   // Only for outdoor training
        Double longitude   // Only for outdoor training
) {
}