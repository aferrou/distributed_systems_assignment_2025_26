package gr.hua.dit.fittrack.core.controller;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AvailabilityService;
import gr.hua.dit.fittrack.core.service.model.AvailabilityRequest;
import gr.hua.dit.fittrack.core.service.model.AvailabilityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService service;
    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;

    public AvailabilityController(
            AvailabilityService service,
            CurrentUserProvider currentUserProvider,
            PersonRepository personRepository) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
    }

    @GetMapping
    public List<AvailabilityResponse> getTrainerAvailability() {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getType() != PersonType.TRAINER) {
            return List.of();
        }
        return service.getTrainerAvailability(trainer);
    }

    @PostMapping
    public ResponseEntity<?> saveAvailability(@RequestBody AvailabilityRequest dto) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getType() != PersonType.TRAINER)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only trainers can save availability");

        try {
            AvailabilityResponse saved = service.createAvailability(trainer, dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityRequest dto) {

        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getType() != PersonType.TRAINER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only trainers can update availability");
        }

        try {
            service.updateAvailability(trainer, id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteAvailability(@PathVariable Long id) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getType() != PersonType.TRAINER)
            return "Error: Only trainers can delete availability";

        return service.deleteAvailability(trainer, id);
    }
}
