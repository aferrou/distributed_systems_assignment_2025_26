package gr.hua.dit.fittrack.web.rest.model;

import gr.hua.dit.fittrack.web.rest.ClientAuthResource;

/**
 * @see ClientAuthResource
 */
public record ClientTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
