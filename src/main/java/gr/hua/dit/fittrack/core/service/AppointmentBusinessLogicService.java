package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.service.model.CompleteAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.CreateAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.StartAppointmentRequest;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Appointment}.
 *
 * <p><strong>All methods MUST be {@link CurrentUser}-aware.</strong></p>
 */
public interface AppointmentBusinessLogicService {

    Optional<AppointmentView> getAppointment(final Long id);

    List<AppointmentView> getAppointments();

    AppointmentView createAppointment(final CreateAppointmentRequest createAppointmentRequest, final boolean notify);

    default AppointmentView createAppointment(final CreateAppointmentRequest createAppointmentRequest) {
        return this.createAppointment(createAppointmentRequest, true);
    }

    AppointmentView startAppointment(final StartAppointmentRequest startAppointmentRequest);

    AppointmentView completeAppointment(final CompleteAppointmentRequest completeAppointmentRequest);
}
