package gr.hua.dit.fittrack.web.ui.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteAppointmentForm(
        @NotNull @NotBlank @Size(max = 1000) String trainerNotes
) {}
