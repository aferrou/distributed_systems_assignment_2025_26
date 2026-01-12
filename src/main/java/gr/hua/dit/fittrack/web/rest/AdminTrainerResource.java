package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.service.PersonBusinessLogicService;
import gr.hua.dit.fittrack.core.service.model.CreatePersonRequest;
import gr.hua.dit.fittrack.core.service.model.CreatePersonResult;
import gr.hua.dit.fittrack.core.service.model.PersonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/trainers")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Trainers", description = "Admin endpoints for trainer management")
public class AdminTrainerResource {

    private final PersonBusinessLogicService personService;

    public AdminTrainerResource(final PersonBusinessLogicService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }

    /**
     * POST /api/v1/admin/trainers : Create a new trainer
     */
    @Operation(
            summary = "Create trainer account",
            description = "Creates a new trainer account. Admin only.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PersonView createTrainer(@Valid @RequestBody final CreatePersonRequest request) {

        // SECURITY: Force TRAINER type (in case client sends wrong type)
        final CreatePersonRequest trainerRequest = new CreatePersonRequest(
                PersonType.TRAINER,  // ← FORCE TRAINER
                request.username(),
                request.firstName(),
                request.lastName(),
                request.emailAddress(),
                request.rawPassword(),
                request.specialisation(),
                request.trainArea()
        );

        final CreatePersonResult result = personService.createTrainer(trainerRequest, true);

        if (!result.success()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    result.errorMessage()
            );
        }

        return result.personView();
    }
}
