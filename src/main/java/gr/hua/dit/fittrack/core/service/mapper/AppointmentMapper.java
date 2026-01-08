package gr.hua.dit.fittrack.core.service.mapper;

import gr.hua.dit.fittrack.core.model.Appointment;

import gr.hua.dit.fittrack.core.model.TrainingType;
import gr.hua.dit.fittrack.core.port.WeatherPort;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import gr.hua.dit.fittrack.core.service.model.PersonView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Mapper to convert {@link Appointment} to {@link AppointmentView}.
 */
@Component
public class AppointmentMapper {

    private final PersonMapper personMapper;
    private final WeatherPort weatherPort;

    public AppointmentMapper(final PersonMapper personMapper, WeatherPort weatherPort) {
        if (personMapper == null) throw new NullPointerException();
        if (weatherPort == null) throw new NullPointerException();
        this.personMapper = personMapper;
        this.weatherPort = weatherPort;
    }

    public AppointmentView convertAppointmentToAppointmentView(final Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        PersonView userView = personMapper.convertPersonToPersonView(appointment.getUser());
        PersonView trainerView = personMapper.convertPersonToPersonView(appointment.getTrainer());

        WeatherForecast forecast = null;
        if (appointment.getTrainingType() == TrainingType.OUTDOOR_TRAINING &&
                appointment.getLatitude() != null &&
                appointment.getLongitude() != null &&
                appointment.getScheduledAt() != null) {

            LocalDate date = LocalDate.ofInstant(
                    appointment.getScheduledAt(),
                    ZoneId.systemDefault()
            );

            try {
                forecast = weatherPort.getForecast(
                        appointment.getLatitude(),
                        appointment.getLongitude(),
                        date
                );
            } catch (Exception e) {
                // Log but don't fail the mapping
                forecast = null;
            }
        }

        return new AppointmentView(
                appointment.getId(),
                userView,
                trainerView,
                appointment.getStatus(),
                appointment.getTrainingType(),
                appointment.getUserNotes(),
                appointment.getTrainerNotes(),
                appointment.getScheduledAt(),
                appointment.getCreatedAt(),
                appointment.getConfirmedAt(),
                appointment.getCompletedAt(),
                appointment.getLatitude(),
                appointment.getLongitude(),
                forecast
        );
    }
}
