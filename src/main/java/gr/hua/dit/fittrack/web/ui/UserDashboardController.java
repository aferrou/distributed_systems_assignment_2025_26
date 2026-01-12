package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.Goal;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.model.Progress;
import gr.hua.dit.fittrack.core.model.TrainingType;
import gr.hua.dit.fittrack.core.repository.GoalRepository;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.repository.ProgressRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import gr.hua.dit.fittrack.core.service.AppointmentBusinessLogicService;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.AppointmentView;
import gr.hua.dit.fittrack.core.service.model.PersonView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user-dashboard")
public class UserDashboardController {

    private final ProgressRepository progressRepository;
    private final GoalRepository goalRepository;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final CurrentUserProvider currentUserProvider;
    private final AppointmentBusinessLogicService appointmentService;

    public UserDashboardController(ProgressRepository progressRepository,
                                   GoalRepository goalRepository,
                                   PersonRepository personRepository,
                                   PersonMapper personMapper,
                                   CurrentUserProvider currentUserProvider,
                                   AppointmentBusinessLogicService appointmentService) {
        this.progressRepository = progressRepository;
        this.goalRepository = goalRepository;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.currentUserProvider = currentUserProvider;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String showUserDashboard(Model model, Authentication authentication) {
        if (!AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Long userId = currentUser.id();

        // Load current user info
        Person person = personRepository.findById(userId).orElse(null);
        if (person != null) {
            model.addAttribute("user", personMapper.convertPersonToPersonView(person));
        }

        // Load trainers for appointment booking
        List<PersonView> trainers = personRepository
                .findAllByTypeOrderByLastName(PersonType.TRAINER)
                .stream()
                .map(personMapper::convertPersonToPersonView)
                .toList();
        model.addAttribute("trainers", trainers);

        // Load user's appointments
        List<AppointmentView> appointments = appointmentService.getAppointments();
        model.addAttribute("appointments", appointments);

        // Load goals
        List<Goal> userGoals = goalRepository.findByUserId(userId);
        List<String> goalNames = userGoals.stream()
                .map(Goal::getGoalName)
                .collect(Collectors.toList());
        model.addAttribute("goals", goalNames);

        // Load progress entries
        List<Progress> progressEntries = progressRepository.findByUserIdOrderByDateDesc(userId);
        model.addAttribute("progressEntries", progressEntries);

        return "user_dashboard";
    }

    @PostMapping("/add-goal")
    public String addGoal(@RequestParam("goal") String goalName,
                          RedirectAttributes redirectAttributes,
                          Authentication authentication) {
        if (!AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        Long userId = 1L;  // Placeholder

        Goal goal = new Goal(userId, goalName);
        goalRepository.save(goal);

        redirectAttributes.addFlashAttribute("goalMessage", "Ο στόχος προστέθηκε επιτυχώς!");

        return "redirect:/user-dashboard";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@RequestParam("date") String dateStr,
                                  @RequestParam("time") String timeStr,
                                  @RequestParam("trainingType") String trainingType,
                                  @RequestParam("trainerId") String trainerId,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {
        if (!AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        // This would integrate with your existing appointment system
        redirectAttributes.addFlashAttribute("appointmentMessage",
                "Το ραντεβού σας καταχωρήθηκε επιτυχώς! (Integration pending)");

        return "redirect:/user-dashboard";
    }

    @PostMapping("/add-progress")
    public String addProgress(@RequestParam("date") String dateStr,
                              @RequestParam(value = "weight", required = false) Double weight,
                              @RequestParam(value = "workoutDuration", required = false) Integer workoutDuration,
                              @RequestParam(value = "trainingType", required = false) String trainingTypeStr,
                              @RequestParam(value = "notes", required = false) String notes,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        if (!AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        Long userId = currentUser.id();
        LocalDate date = LocalDate.parse(dateStr);

        TrainingType trainingType = null;
        if (trainingTypeStr != null && !trainingTypeStr.isEmpty()) {
            try {
                trainingType = TrainingType.valueOf(trainingTypeStr);
            } catch (IllegalArgumentException e) {
                // Invalid training type, leave as null
            }
        }

        Progress progress = new Progress(
                userId,
                date,
                weight,
                workoutDuration,
                trainingType,
                notes
        );

        progressRepository.save(progress);

        redirectAttributes.addFlashAttribute("progressMessage", "Η πρόοδός σας καταγράφηκε επιτυχώς!");

        return "redirect:/user-dashboard";
    }
}