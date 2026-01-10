package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.service.AppointmentDataService;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing {@code Appointment} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentResource {

    private final AppointmentDataService appointmentDataService;

    public AppointmentResource(final AppointmentDataService appointmentDataService) {
        if (appointmentDataService == null) throw new NullPointerException();
        this.appointmentDataService = appointmentDataService;
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<AppointmentView> appointments() {
        final List<AppointmentView> appointmentViewList = this.appointmentDataService.getAllAppointments();
        return appointmentViewList;
    }
}
