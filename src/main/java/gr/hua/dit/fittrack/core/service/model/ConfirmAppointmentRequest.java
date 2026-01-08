package gr.hua.dit.fittrack.core.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConfirmAppointmentRequest(@NotNull @Positive Long id) {}