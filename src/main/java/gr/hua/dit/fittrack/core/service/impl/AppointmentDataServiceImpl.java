package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.service.AppointmentDataService;
import gr.hua.dit.fittrack.core.service.mapper.AppointmentMapper;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link AppointmentDataService}.
 */
@Service
public class AppointmentDataServiceImpl implements AppointmentDataService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentDataServiceImpl(final AppointmentRepository appointmentRepository,
                                 final AppointmentMapper appointmentMapper) {
        if (appointmentRepository == null) throw new NullPointerException();
        if (appointmentMapper == null) throw new NullPointerException();
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public List<AppointmentView> getAllAppointments() {
        final List<Appointment> appointmentList = this.appointmentRepository.findAll();
        final List<AppointmentView> appointmentViewList = appointmentList
                .stream()
                .map(this.appointmentMapper::convertAppointmentToAppointmentView)
                .toList();
        return appointmentViewList;
    }
}
