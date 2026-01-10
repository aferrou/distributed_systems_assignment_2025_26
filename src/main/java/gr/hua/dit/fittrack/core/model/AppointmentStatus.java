package gr.hua.dit.fittrack.core.model;

public enum AppointmentStatus {
    REQUESTED,      // User requested, waiting for trainer confirmation
    CONFIRMED,      // Trainer confirmed the appointment
    IN_PROGRESS,    // Appointment is happening
    COMPLETED,      // Appointment finished
    CANCELLED       // Appointment cancelled
}

//Fix AppointmentBusinessLogicServiceImpl, trainer must confirm the appointment request