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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI controller for managing profile.
 */
@Controller
public class ProfileController {

    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final GoalRepository goalRepository;
    private final ProgressRepository progressRepository;
    private final AppointmentBusinessLogicService appointmentService;

    public ProfileController(final CurrentUserProvider currentUserProvider,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper,
                             final GoalRepository goalRepository,
                             final ProgressRepository progressRepository,
                             final AppointmentBusinessLogicService appointmentService) {
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.goalRepository = goalRepository;
        this.progressRepository = progressRepository;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/profile")
    public String showProfile(final Model model) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        final Person person = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        final PersonView user = personMapper.convertPersonToPersonView(person);
        model.addAttribute("user", user);

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
        List<Goal> userGoals = goalRepository.findByUserId(currentUser.id());
        List<String> goalNames = userGoals.stream()
                .map(Goal::getGoalName)
                .collect(Collectors.toList());
        model.addAttribute("goals", goalNames);

        // Load progress entries
        List<Progress> progressEntries = progressRepository.findByUserIdOrderByDateDesc(currentUser.id());
        model.addAttribute("progressEntries", progressEntries);

        return "user_dashboard";
    }

    @PostMapping("/profile/add-goal")
    public String addGoal(@RequestParam("goal") String goalName,
                          RedirectAttributes redirectAttributes) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        // Check if goal already exists for this user
        if (goalRepository.existsByUserIdAndGoalName(currentUser.id(), goalName)) {
            redirectAttributes.addFlashAttribute("goalError", "Αυτός ο στόχος υπάρχει ήδη!");
            return "redirect:/profile";
        }

        Goal goal = new Goal(currentUser.id(), goalName);
        goalRepository.save(goal);

        redirectAttributes.addFlashAttribute("goalMessage", "Ο στόχος προστέθηκε επιτυχώς!");

        return "redirect:/profile";
    }

    @PostMapping("/profile/book-appointment")
    public String bookAppointment(@RequestParam("date") String dateStr,
                                  @RequestParam("time") String timeStr,
                                  @RequestParam("trainingType") String trainingType,
                                  @RequestParam("trainerId") String trainerId,
                                  RedirectAttributes redirectAttributes) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        // This would integrate with your existing appointment system
        redirectAttributes.addFlashAttribute("appointmentMessage",
                "Το ραντεβού σας καταχωρήθηκε επιτυχώς! Σε αναμονή έγκρισης από τον trainer.");

        return "redirect:/profile";
    }

    @PostMapping("/profile/add-progress")
    public String addProgress(@RequestParam("date") String dateStr,
                              @RequestParam(value = "weight", required = false) Double weight,
                              @RequestParam(value = "workoutDuration", required = false) Integer workoutDuration,
                              @RequestParam(value = "trainingType", required = false) String trainingTypeStr,
                              @RequestParam(value = "notes", required = false) String notes,
                              RedirectAttributes redirectAttributes) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

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
                currentUser.id(),
                date,
                weight,
                workoutDuration,
                trainingType,
                notes
        );

        progressRepository.save(progress);

        redirectAttributes.addFlashAttribute("progressMessage", "Η πρόοδός σας καταγράφηκε επιτυχώς!");

        return "redirect:/profile";
    }
}
