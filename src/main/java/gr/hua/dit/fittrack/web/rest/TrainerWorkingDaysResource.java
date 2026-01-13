package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for getting trainer working days.
 */
@RestController
@RequestMapping("/api/trainers")
public class TrainerWorkingDaysResource {

    private final PersonRepository personRepository;

    public TrainerWorkingDaysResource(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Get the working days for a specific trainer.
     * Returns a list of day numbers (1=Monday, 7=Sunday).
     */
    @GetMapping("/{trainerId}/working-days")
    public ResponseEntity<List<Integer>> getTrainerWorkingDays(@PathVariable Long trainerId) {
        if (trainerId == null) {
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Person trainer = personRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        if (trainer.getWorkingDays() == null || trainer.getWorkingDays().isBlank()) {
            // If no working days defined, return empty list (or all days)
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Parse "1,2,4,5" into [1, 2, 4, 5]
        List<Integer> workingDays = java.util.Arrays.stream(trainer.getWorkingDays().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();

        return ResponseEntity.ok(workingDays);
    }
}
