/*package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;
import gr.hua.dit.fittrack.core.service.model.PersonView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final AppointmentBusinessLogicService appointmentService;

    public TrainerController(
            CurrentUserProvider currentUserProvider,
            PersonRepository personRepository,
            PersonMapper personMapper,
            AppointmentBusinessLogicService appointmentService) {
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String showTrainerDashboard(Model model) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        PersonView trainerView = personMapper.convertPersonToPersonView(trainer);
        model.addAttribute("trainer", trainerView);

        // Fetch appointments for this trainer
        List<AppointmentView> appointments = appointmentService.getAppointments();
        model.addAttribute("appointments", appointments);

        return "trainer";
    }
}*/
package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.repository.TrainerNoteRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;
import gr.hua.dit.fittrack.core.service.model.PersonView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final AppointmentBusinessLogicService appointmentService;

    // ✅ new
    private final AppointmentRepository appointmentRepository;
    private final TrainerNoteRepository trainerNoteRepository;

    public TrainerController(
            CurrentUserProvider currentUserProvider,
            PersonRepository personRepository,
            PersonMapper personMapper,
            AppointmentBusinessLogicService appointmentService,
            AppointmentRepository appointmentRepository,
            TrainerNoteRepository trainerNoteRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.trainerNoteRepository = trainerNoteRepository;
    }

    @GetMapping
    public String showTrainerDashboard(@RequestParam(value = "userId", required = false) Long userId,
                                       Model model) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        PersonView trainerView = personMapper.convertPersonToPersonView(trainer);
        model.addAttribute("trainer", trainerView);

        // Existing: appointments
        List<AppointmentView> appointments = appointmentService.getAppointments();
        model.addAttribute("appointments", appointments);

        // ✅ Notes: users who chose this trainer
        List<Person> users = appointmentRepository.findDistinctUsersForTrainer(
                trainer.getId(),
                List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED)
        );
        model.addAttribute("users", users);
        model.addAttribute("selectedUserId", userId);

        // ✅ Notes list for selected user
        if (userId != null) {
            model.addAttribute("notes",
                    trainerNoteRepository.findByTrainer_IdAndUser_IdOrderByCreatedAtDesc(trainer.getId(), userId)
            );
        } else {
            model.addAttribute("notes", List.of());
        }

        return "trainer";
    }
}

