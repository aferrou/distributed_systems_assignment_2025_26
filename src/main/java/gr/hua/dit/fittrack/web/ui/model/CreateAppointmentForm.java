package gr.hua.dit.fittrack.web.ui.model;

import gr.hua.dit.fittrack.core.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record CreateAppointmentForm(
        @NotNull @Positive Long trainerId,
        @NotNull TrainingType trainingType,
        @NotNull Instant scheduledAt,
        @NotBlank @Size(max = 1000) String userNotes,
        // For outdoor training
        Double latitude,
        Double longitude
) {
}
