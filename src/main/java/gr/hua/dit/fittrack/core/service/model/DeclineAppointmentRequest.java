package gr.hua.dit.fittrack.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeclineAppointmentRequest(
        @NotNull Long id,
        @NotBlank String declineReason
) {}
