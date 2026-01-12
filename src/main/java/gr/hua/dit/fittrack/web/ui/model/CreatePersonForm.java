package gr.hua.dit.fittrack.web.ui.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePersonForm(

        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String rawPassword
) {}