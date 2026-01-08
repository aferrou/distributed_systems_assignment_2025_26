package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.AppointmentDataService;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.ConfirmAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for managing {@code Appointment} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Appointments", description = "Appointment management API")
public class AppointmentResource {

    private final AppointmentBusinessLogicService appointmentBusinessLogicService;

    public AppointmentResource(final AppointmentBusinessLogicService appointmentBusinessLogicService) {
        if (appointmentBusinessLogicService == null) throw new NullPointerException();
        this.appointmentBusinessLogicService = appointmentBusinessLogicService;
    }

    @Operation(summary = "Get current user's appointments")
    @GetMapping
    public List<AppointmentView> getMyAppointments() {
        return this.appointmentBusinessLogicService.getAppointments();
    }

    @Operation(summary = "Get appointment by ID")
    @GetMapping("/{id}")
    public AppointmentView getAppointment(@PathVariable final Long id) {
        return this.appointmentBusinessLogicService.getAppointment(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    @Operation(summary = "Create new appointment")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentView createAppointment(
            @Valid @RequestBody final CreateAppointmentRequest request) {
        return this.appointmentBusinessLogicService.requestAppointment(request, true);
    }

    @Operation(summary = "Confirm appointment (trainer only)")
    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/{id}/confirm")
    public AppointmentView confirmAppointment(@PathVariable final Long id) {
        return this.appointmentBusinessLogicService.confirmAppointment(
                new ConfirmAppointmentRequest(id));
    }

    @Operation(summary = "Complete appointment (trainer only)")
    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping(value = "/{id}/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentView completeAppointment(
            // @PathVariable final Long id,
            @Valid @RequestBody final CompleteAppointmentRequest request) {
        return this.appointmentBusinessLogicService.completeAppointment(request);
    }
}