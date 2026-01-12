package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.ConfirmAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;
import gr.hua.dit.fittrack.core.service.model.PersonView;

import gr.hua.dit.fittrack.web.ui.model.CompleteAppointmentForm;
import gr.hua.dit.fittrack.web.ui.model.CreateAppointmentForm;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import java.util.List;

/**
 * UI controller for managing appointments.
 */
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentBusinessLogicService appointmentService;
    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public AppointmentController(
            final AppointmentBusinessLogicService appointmentService,
            final CurrentUserProvider currentUserProvider,
            final PersonRepository personRepository,
            final PersonMapper personMapper) {
        if (appointmentService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        this.appointmentService = appointmentService;
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    private List<PersonView> getTrainers() {
        return personRepository
                .findAllByTypeOrderByLastName(PersonType.TRAINER)
                .stream()
                .map(personMapper::convertPersonToPersonView)
                .toList();
    }

    // List appointments
    // --------------------------------------------------

    @GetMapping
    public String list(final Model model) {
        final List<AppointmentView> appointments = appointmentService.getAppointments();
        model.addAttribute("appointments", appointments);
        return "appointments";
    }

    // Appointment details
    // --------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable final Long id, final Model model) {

        final AppointmentView appointment = appointmentService
                .getAppointment(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment not found"));

        model.addAttribute("appointment", appointment);
        model.addAttribute("completeForm", new CompleteAppointmentForm(""));

        return "appointment";
    }

    // Create appointment (USER's side)
    // --------------------------------------------------

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/new")
    public String showCreateForm(final Model model) {
        model.addAttribute("form", new CreateAppointmentForm(null, null, null, "", null, null));
        model.addAttribute("trainers", getTrainers());
        return "new_appointment";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/new")
    @ResponseBody
    public ResponseEntity<?> handleCreateForm(
            @ModelAttribute("form") @Valid final CreateAppointmentForm form,
            final BindingResult bindingResult,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith
    ) {
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        if (bindingResult.hasErrors()) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", "Παρακαλώ συμπληρώστε όλα τα πεδία"));
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/appointments/new")
                    .build();
        }

        try {
            final CreateAppointmentRequest request =
                    new CreateAppointmentRequest(
                            this.currentUserProvider.requireUserId(),
                            form.trainerId(),
                            form.getTrainingTypeOrDefault(),
                            form.userNotes() != null ? form.userNotes() : "",
                            form.getScheduledAtAsInstant(),
                            form.latitude(),
                            form.longitude()
                    );

            final AppointmentView appointment =
                    appointmentService.requestAppointment(request, true);

            if (isAjax) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Το ραντεβού είναι σε αναμονή έγκρισης"));
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/appointments/" + appointment.id())
                    .build();
        } catch (IllegalStateException e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/appointments/new?error=" + e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/appointments/new?error=" + e.getMessage())
                    .build();
        }
    }

    // Confirm appointment (TRAINER's side)
    // --------------------------------------------------

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirm(
            @PathVariable final Long id,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith
    ) {
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        final AppointmentView appointment =
                appointmentService.confirmAppointment(
                        new ConfirmAppointmentRequest(id)
                );

        if (isAjax) {
            return ResponseEntity.ok(Map.of("success", true, "status", "CONFIRMED"));
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/appointments/" + appointment.id())
                .build();
    }

    @PostMapping("/{id}/complete")
    public String complete(
            @PathVariable final Long id,
            @ModelAttribute("completeForm") @Valid final CompleteAppointmentForm form,
            final BindingResult bindingResult,
            final Model model
    ) {
        if (bindingResult.hasErrors()) {
            // Need to reload the appointment for the view
            final AppointmentView appointment = appointmentService
                    .getAppointment(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Appointment not found"));
            model.addAttribute("appointment", appointment);
            return "appointment";
        }

        final AppointmentView appointment =
                appointmentService.completeAppointment(
                        new CompleteAppointmentRequest(id, form.trainerNotes())
                );

        return "redirect:/appointments/" + appointment.id();
    }
}