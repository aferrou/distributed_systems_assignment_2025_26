package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.service.AppointmentDataService;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin Appointments", description = "Administrative appointment access")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentResource {

    private final AppointmentDataService appointmentDataService ;

    public AdminAppointmentResource(final AppointmentDataService appointmentDataService) {
        if (appointmentDataService == null) throw new NullPointerException();
        this.appointmentDataService = appointmentDataService;
    }

    @Operation(
            summary = "Get all appointments",
            // Requires ADMIN or INTEGRATION role.
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATION_READ')")
    @GetMapping()
    public List<AppointmentView> getAllAppointments() {
        return this.appointmentDataService.getAllAppointments();
    }
}
