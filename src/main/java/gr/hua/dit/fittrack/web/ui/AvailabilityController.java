package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.Availability;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.AvailabilityRepository;
import gr.hua.dit.fittrack.core.service.AvailabilityService;
import gr.hua.dit.fittrack.core.service.mapper.AvailabilityMapper;
import gr.hua.dit.fittrack.core.service.model.AvailabilityRequest;
import gr.hua.dit.fittrack.core.service.model.AvailabilityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {


    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) {
        this.service = service;
    }


    // Load availability for logged trainer
    @GetMapping
    //chnage Availability
    public List<AvailabilityResponse> getTrainerAvailability(HttpSession session) {
        Person trainer = (Person) session.getAttribute("authenticatedPerson");

        if (trainer == null || trainer.getType() != PersonType.TRAINER) {
            return List.of();
        }
        return service.getTrainerAvailability(trainer);
    }
    /*@PostMapping
    public AvailabilityResponse saveAvailability(@RequestBody AvailabilityRequest dto, HttpSession session) {
        Person trainer = (Person) session.getAttribute("authenticatedPerson");
        if (trainer == null || trainer.getType() != PersonType.TRAINER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trainers can save availability");
        return service.createAvailability(trainer, dto);
    }*/
    @PostMapping
    public ResponseEntity<?> saveAvailability(@RequestBody AvailabilityRequest dto, HttpSession session) {
        Person trainer = (Person) session.getAttribute("authenticatedPerson");
        if (trainer == null || trainer.getType() != PersonType.TRAINER)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only trainers can save availability");

        try {
            AvailabilityResponse saved = service.createAvailability(trainer, dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // <-- return 400 + message
        }
    }
    /* @PutMapping("/{id}")
     public String updateAvailability(@PathVariable Long id, @RequestBody AvailabilityRequest dto, HttpSession session) {
         Person trainer = (Person) session.getAttribute("authenticatedPerson");
         if (trainer == null || trainer.getType() != PersonType.TRAINER) return "Error: Only trainers can update availability";
         return service.updateAvailability(trainer, id, dto);
     }*/
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityRequest dto,
            HttpSession session) {

        Person trainer = (Person) session.getAttribute("authenticatedPerson");
        if (trainer == null || trainer.getType() != PersonType.TRAINER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only trainers can update availability");
        }

        try {
            service.updateAvailability(trainer, id, dto);
            return ResponseEntity.ok().build(); // 200 OK, no body needed
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 + message
        }
    }

    @DeleteMapping("/{id}")
    public String deleteAvailability(@PathVariable Long id, HttpSession session) {
        Person trainer = (Person) session.getAttribute("authenticatedPerson");
        if (trainer == null || trainer.getType() != PersonType.TRAINER) return "Error: Only trainers can delete availability";
        return service.deleteAvailability(trainer, id);
    }

    // Save new availability
   /* @PostMapping
    public String saveAvailability(@RequestBody AvailabilityRequest dto, HttpSession session) {
       /* Person trainer = (Person) session.getAttribute("authenticatedPerson");
        if (trainer == null || trainer.getType() != PersonType.TRAINER) {
            return "Error: Only trainers can save availability";
        }


        if (availability.getStartTime().isBefore(OffsetDateTime.now())) {
            return "Error: Cannot create availability in the past";
        }

        availability.setTrainer(trainer);
        repository.save(availability);
        return "Saved";
        Person trainer = (Person) session.getAttribute("authenticatedPerson");

        // Only trainers can save
        if (trainer == null || trainer.getType() != PersonType.TRAINER) {
            return "Error: Only trainers can save availability";
        }

        // Validate input
        if (dto.startTime() == null || dto.endTime() == null) {
            return "Error: Start and end time are required";
        }

        if (!dto.endTime().isAfter(dto.startTime())) {
            return "Error: End time must be after start time";
        }

        if (dto.startTime().isBefore(OffsetDateTime.now())) {
            return "Error: Cannot create availability in the past";
        }

        // Map DTO → entity and save
        repository.save(
                availabilityMapper.toEntity(dto, trainer)
        );

        return "Saved";
    }*/
}
