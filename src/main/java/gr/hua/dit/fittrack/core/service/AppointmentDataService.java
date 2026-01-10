package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import java.util.List;

/**
 * Service for managing {@code Ticket} for data analytics purposes.
 */
public interface AppointmentDataService {
    List<AppointmentView> getAllAppointments();
}
