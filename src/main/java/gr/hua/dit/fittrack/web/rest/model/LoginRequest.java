package gr.hua.dit.fittrack.web.rest.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for user login via REST API.
 */
public record LoginRequest(
        @NotNull @NotBlank @Email String email,
        @NotNull @NotBlank String password
) {}
