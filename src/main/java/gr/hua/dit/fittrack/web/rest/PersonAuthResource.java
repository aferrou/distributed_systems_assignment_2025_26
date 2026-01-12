package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.security.JwtService;
import gr.hua.dit.fittrack.web.rest.model.LoginRequest;
import gr.hua.dit.fittrack.web.rest.model.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for person (user/trainer) authentication.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication endpoints for users and trainers")
public class PersonAuthResource {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public PersonAuthResource(final PersonRepository personRepository,
                              final PasswordEncoder passwordEncoder,
                              final JwtService jwtService) {
        if (personRepository == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (jwtService == null) throw new NullPointerException();
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Login with email and password", description = "Authenticate a user/trainer and receive a JWT token")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@RequestBody @Valid final LoginRequest loginRequest) {
        final String email = loginRequest.email();
        final String password = loginRequest.password();

        // Find person by email
        final Person person = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(password, person.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Generate JWT token with person claims
        final String role = "ROLE_" + person.getType().name();
        final String token = jwtService.issueForPerson(
                person.getId(),
                person.getEmailAddress(),
                person.getType().name(),
                List.of(role)
        );

        return new LoginResponse(
                token,
                "Bearer",
                3600, // 1 hour in seconds
                person.getId(),
                person.getEmailAddress(),
                person.getFirstName(),
                person.getLastName(),
                person.getType()
        );
    }
}
