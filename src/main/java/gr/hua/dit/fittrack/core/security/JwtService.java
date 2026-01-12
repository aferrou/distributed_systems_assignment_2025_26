package gr.hua.dit.fittrack.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

/**
 * JWT (JSON Web Token) service.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final String audience;
    private final long ttlMinutes; // time-to-live.

    public JwtService(@Value("${app.jwt.secret}") final String secret,
                      @Value("${app.jwt.issuer}") final String issuer,
                      @Value("${app.jwt.audience}") final String audience,
                      @Value("${app.jwt.ttl-minutes}") final long ttlMinutes) {
        if (secret == null) throw new NullPointerException();
        if (secret.isBlank()) throw new IllegalArgumentException();
        if (issuer == null) throw new NullPointerException();
        if (issuer.isBlank()) throw new IllegalArgumentException();
        if (audience == null) throw new NullPointerException();
        if (audience.isBlank()) throw new IllegalArgumentException();
        if (ttlMinutes <= 0) throw new IllegalArgumentException();
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.ttlMinutes = ttlMinutes;
    }

    public String issue(final String subject, final Collection<String> roles) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuer(this.issuer)
                .audience().add(this.audience).and()
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(this.ttlMinutes))))
                .signWith(this.key)
                .compact();
    }

    /**
     * Issues a JWT token for a person with additional claims.
     */
    public String issueForPerson(final long personId, final String email, final String type, final Collection<String> roles) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuer(this.issuer)
                .audience().add(this.audience).and()
                .claim("personId", personId)
                .claim("type", type)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(this.ttlMinutes))))
                .signWith(this.key)
                .compact();
    }

    public Claims parse(final String token) {
        return Jwts.parser()
                .requireAudience(this.audience)
                .requireIssuer(this.issuer)
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}