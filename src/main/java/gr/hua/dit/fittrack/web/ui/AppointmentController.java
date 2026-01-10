package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.model.*;

import gr.hua.dit.fittrack.web.ui.model.CompleteAppointmentForm;
import gr.hua.dit.fittrack.web.ui.model.CreateAppointmentForm;

import jakarta.validation.Valid;

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

import java.time.ZoneId;
import java.util.List;

/**
 * UI controller for managing appointments.
 */
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentBusinessLogicService appointmentBusinessLogicService;
    private final CurrentUserProvider currentUserProvider;

    public AppointmentController(final AppointmentBusinessLogicService appointmentBusinessLogicService, final CurrentUserProvider currentUserProvider) {
        if (appointmentBusinessLogicService == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.appointmentBusinessLogicService = appointmentBusinessLogicService;
        this.currentUserProvider = currentUserProvider;
    }

    // List appointments
    // --------------------------------------------------

    @GetMapping("")
    public String list(final Model model) {
        final List<AppointmentView> appointmentViewList = this.appointmentBusinessLogicService.getAppointments();
        model.addAttribute("appointments", appointmentViewList);
        return "appointments";
    }

    // Appointment details
    // --------------------------------------------------

    @GetMapping("/{appointmentId}")
    public String detail(@PathVariable final Long appointmentId, final Model model) {
        final AppointmentView appointmentView = this.appointmentBusinessLogicService.getAppointment(appointmentId).orElse(null);
        if (appointmentId == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Appointment not found");
        }
        final CompleteAppointmentForm completeAppointmentForm = new CompleteAppointmentForm("");
        model.addAttribute("appointment", appointmentView);
        model.addAttribute("completeAppointmentForm", completeAppointmentForm);
        return "appointment";
    }

    // Create appointments
    // --------------------------------------------------

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/new")
    public String showCreateForm(final Model model) {
        // form initial data
        final CreateAppointmentForm createAppointmentForm = new CreateAppointmentForm(null, null, "", null);
        model.addAttribute("form", createAppointmentForm);
        return "new_appointment";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/new")
    public String handleCreateForm(
            @ModelAttribute("form") @Valid final CreateAppointmentForm createAppointmentForm,
            final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "new_appointment";
        }
        final CreateAppointmentRequest createAppointmentRequest = new CreateAppointmentRequest(
                this.currentUserProvider.requireUserId(), // The current user must be User. We need their ID.
                createAppointmentForm.trainerId(),
                createAppointmentForm.trainingType(),
                createAppointmentForm.userNotes(),
                createAppointmentForm.scheduledAt().atZone(ZoneId.systemDefault()).toInstant(),
                null,
                null
        );
        final AppointmentView appointmentView = this.appointmentBusinessLogicService.createAppointment(createAppointmentRequest);
        return "redirect:/appointments/" + appointmentView.id();
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{appointmentId}/start")
    public String handleStartForm(@PathVariable final Long appointmentId) {
        final StartAppointmentRequest startAppointmentRequest = new StartAppointmentRequest(appointmentId);
        final AppointmentView appointmentView = this.appointmentBusinessLogicService.startAppointment(startAppointmentRequest);
        return "redirect:/appointments/" + appointmentView.id();
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{appointmentId}/complete")
    public String handleCompleteForm(
            @PathVariable final Long appointmentId,
            @ModelAttribute("form") final CompleteAppointmentForm completeAppointmentForm,
            final BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "appointment";
        }
        final CompleteAppointmentRequest completeAppointmentRequest = new CompleteAppointmentRequest(
                appointmentId,
                completeAppointmentForm.trainerNotes()
        );
        final AppointmentView appointmentView = this.appointmentBusinessLogicService.completeAppointment(completeAppointmentRequest);
        return "redirect:/appointments/" + appointmentView.id();
    }

//    // Confirm appointment endpoint
//    @PreAuthorize("hasRole('TRAINER')")
//    @PostMapping("/{appointmentId}/confirm")
//    public String handleConfirmForm(
//            @PathVariable final Long appointmentId,
//            @ModelAttribute("form") @Valid final ConfirmAppointmentForm confirmAppointmentForm,
//            final BindingResult bindingResult
//    ) {
//        if (bindingResult.hasErrors()) {
//            return "appointment"; // Return to appointment details page
//        }
//        final ConfirmAppointmentRequest confirmRequest = new ConfirmAppointmentRequest(
//                appointmentId,
//                confirmAppointmentForm.trainerNotes()
//        );
//        final AppointmentView appointmentView = this.appointmentBusinessLogicService.confirmAppointment(confirmRequest);
//        return "redirect:/appointments/" + appointmentView.id();
//    }
//
//    // Decline appointment endpoint
//    @PreAuthorize("hasRole('TRAINER')")
//    @PostMapping("/{appointmentId}/decline")
//    public String handleDeclineForm(
//            @PathVariable final Long appointmentId,
//            @ModelAttribute("form") @Valid final DeclineAppointmentForm declineAppointmentForm,
//            final BindingResult bindingResult
//    ) {
//        if (bindingResult.hasErrors()) {
//            return "appointment"; // Return to appointment details page
//        }
//        final DeclineAppointmentRequest declineRequest = new DeclineAppointmentRequest(
//                appointmentId,
//                declineAppointmentForm.declineReason()
//        );
//        final AppointmentView appointmentView = this.appointmentBusinessLogicService.declineAppointment(declineRequest);
//        return "redirect:/appointments/" + appointmentView.id();
//    }
}
