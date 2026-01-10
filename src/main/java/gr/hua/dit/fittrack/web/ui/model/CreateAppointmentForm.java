package gr.hua.dit.fittrack.web.ui.model;

import gr.hua.dit.fittrack.core.model.TrainingType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateAppointmentForm(
        @NotNull @Positive Long trainerId,
        @NotNull TrainingType trainingType,
        @NotNull @NotBlank @Size(max = 1000) String userNotes,
        @NotNull @Future LocalDateTime scheduledAt
) {}
