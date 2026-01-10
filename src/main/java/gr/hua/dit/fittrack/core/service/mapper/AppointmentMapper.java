package gr.hua.dit.fittrack.core.service.mapper;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.service.model.PersonView;

import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Appointment} to {@link AppointmentView}.
 */
@Component
public class AppointmentMapper {

    private final PersonMapper personMapper;

    public AppointmentMapper(final PersonMapper personMapper) {
        if (personMapper == null) throw new NullPointerException();
        this.personMapper = personMapper;
    }

    public AppointmentView convertAppointmentToAppointmentView(final Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new AppointmentView(
                appointment.getId(),
                this.personMapper.convertPersonToPersonView(appointment.getUser()),
                this.personMapper.convertPersonToPersonView(appointment.getTrainer()),
                appointment.getStatus(),
                appointment.getTrainingType(),
                appointment.getUserNotes(),
                appointment.getTrainerNotes(),
                appointment.getRequestedAt(),
                appointment.getConfirmedAt(),
                appointment.getInProgressAt(),
                appointment.getCompletedAt()
        );
    }
}