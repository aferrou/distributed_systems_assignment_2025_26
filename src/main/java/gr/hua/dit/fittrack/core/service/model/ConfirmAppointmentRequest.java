package gr.hua.dit.fittrack.core.service.model;

import jakarta.validation.constraints.NotNull;

public record ConfirmAppointmentRequest(
        @NotNull Long id,
        String trainerNotes // optional confirmation notes
) {}
