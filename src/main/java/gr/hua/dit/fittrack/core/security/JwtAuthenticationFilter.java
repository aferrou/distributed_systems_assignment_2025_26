package gr.hua.dit.fittrack.core.security;

import gr.hua.dit.fittrack.core.model.PersonType;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(final JwtService jwtService) {
        if (jwtService == null) throw new NullPointerException();
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String path = request.getServletPath();
        // Skip filtering for public auth endpoints
        if (path.equals("/api/v1/auth/client-tokens")) return true;
        if (path.equals("/api/v1/auth/login")) return true;
        return !path.startsWith("/api/v1");
    }

    private void writeUnauthorized(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"invalid_token\"}");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);

        try {
            final Claims claims = jwtService.parse(token);
            final String subject = claims.getSubject();

            // Check if this is a client token (subject starts with "client:")
            if (subject != null && subject.startsWith("client:")) {
                // Client token - extract roles from claims
                @SuppressWarnings("unchecked")
                final java.util.List<String> roles = claims.get("roles", java.util.List.class);
                final java.util.List<GrantedAuthority> authorities;
                if (roles != null) {
                    authorities = roles.stream()
                            .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                            .toList();
                } else {
                    authorities = java.util.Collections.emptyList();
                }

                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Person token - extract personId and type
                final Long personId = claims.get("personId", Long.class);
                final String typeRaw = claims.get("type", String.class);

                if (personId == null || typeRaw == null) {
                    LOGGER.warn("JWT missing personId or type claims");
                    writeUnauthorized(response);
                    return;
                }

                final PersonType personType = PersonType.valueOf(typeRaw);

                final ApplicationUserDetails userDetails =
                        new ApplicationUserDetails(
                                personId,
                                subject,
                                "", // password not needed for JWT auth
                                personType
                        );

                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            LOGGER.warn("JWT authentication failed", ex);
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}