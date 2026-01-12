package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import java.util.List;

/**
 * Data service for appointment-related operations.
 */
public interface AppointmentDataService {
    /**
     * Retrieves all appointments from the database.
     *
     * @return list of all appointments
     */
    List<AppointmentView> getAllAppointments();
}