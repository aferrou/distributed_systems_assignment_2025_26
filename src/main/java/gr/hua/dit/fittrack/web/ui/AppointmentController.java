package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.ConfirmAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import gr.hua.dit.fittrack.web.ui.model.CompleteAppointmentForm;
import gr.hua.dit.fittrack.web.ui.model.CreateAppointmentForm;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * UI controller for managing appointments.
 */
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentBusinessLogicService appointmentService;
    private final CurrentUserProvider currentUserProvider;

    public AppointmentController(final AppointmentBusinessLogicService appointmentService, final CurrentUserProvider currentUserProvider) {
        if (appointmentService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        this.appointmentService = appointmentService;
        this.currentUserProvider = currentUserProvider;
    }

    // List appointments
    // --------------------------------------------------

    @GetMapping
    public String list(final Model model) {
        final List<AppointmentView> appointments = appointmentService.getAppointments();
        model.addAttribute("appointments", appointments);
        return "appointments/list";
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

        return "appointments/detail";
    }

    // Create appointment (USER's side)
    // --------------------------------------------------

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/new")
    public String showCreateForm(final Model model) {
        model.addAttribute("form", new CreateAppointmentForm(null, null, null, ""));
        return "appointments/new";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/new")
    public String handleCreateForm(
            @ModelAttribute("form") @Valid final CreateAppointmentForm form,
            final BindingResult bindingResult,
            final Model model
    ) {
        if (bindingResult.hasErrors()) {
            // Re-populate any dropdowns/lists needed for the form
            return "appointments/new";
        }

        // Get userId from authenticated user
        // --------------------------------------------------

        final long userId = currentUserProvider.requireCurrentUser().id();

        final CreateAppointmentRequest request =
                new CreateAppointmentRequest(
                        userId,
                        form.trainerId(),
                        form.trainingType(),
                        form.userNotes(),
                        form.scheduledAt()
                );

        final AppointmentView appointment =
                appointmentService.requestAppointment(request, true);

        return "redirect:/appointments/" + appointment.id();
    }

    // Confirm appointment (TRAINER's side)
    // --------------------------------------------------

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable final Long id) {

        final AppointmentView appointment =
                appointmentService.confirmAppointment(
                        new ConfirmAppointmentRequest(id)
                );

        return "redirect:/appointments/" + appointment.id();
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
            return "appointments/detail";
        }

        final AppointmentView appointment =
                appointmentService.completeAppointment(
                        new CompleteAppointmentRequest(id, form.trainerNotes())
                );

        return "redirect:/appointments/" + appointment.id();
    }
}