package gr.hua.dit.fittrack.web.rest.model;

import gr.hua.dit.fittrack.core.model.PersonType;

/**
 * Response DTO for user login via REST API.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long personId,
        String email,
        String firstName,
        String lastName,
        PersonType type
) {}
