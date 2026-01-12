package gr.hua.dit.fittrack.web.ui;

import org.springframework.ui.Model;
import gr.hua.dit.fittrack.core.model.AppointmentStatus;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.TrainerNote;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.repository.TrainerNoteRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class TrainerNoteController {
    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;
    private final AppointmentRepository appointmentRepository;
    private final TrainerNoteRepository trainerNoteRepository;
    public TrainerNoteController(CurrentUserProvider currentUserProvider,
                                 PersonRepository personRepository,
                                 AppointmentRepository appointmentRepository,
                                 TrainerNoteRepository trainerNoteRepository) {
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
        this.appointmentRepository = appointmentRepository;
        this.trainerNoteRepository = trainerNoteRepository;
    }

   /* @GetMapping("/trainer")
    public String trainerDashboard(@RequestParam(value = "userId", required = false) Long userId,
                                   Model model) {
        CurrentUser cu = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(cu.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        // users that "chose me" = have appointments with me
        // choose which statuses you consider valid:
        List<Person> users = appointmentRepository.findDistinctUsersForTrainer(
                trainer.getId(),
                List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED)
        );

        model.addAttribute("users", users);
        model.addAttribute("selectedUserId", userId);

        if (userId != null) {
            List<TrainerNote> notes = trainerNoteRepository
                    .findByTrainer_IdAndUser_IdOrderByCreatedAtDesc(trainer.getId(), userId);
            model.addAttribute("notes", notes);
        } else {
            model.addAttribute("notes", List.of());
        }

        return "trainer"; // your trainer.html
    }*/

    @PostMapping("/trainer/notes")
    public String addNote(@RequestParam("userId") Long userId,
                          @RequestParam("content") String content,
                          RedirectAttributes ra) {
        CurrentUser cu = currentUserProvider.requireCurrentUser();
        Person trainer = personRepository.findById(cu.id())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        Person user = personRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Optional safety: ensure selected user is actually "my user"
        boolean allowed = appointmentRepository.findDistinctUsersForTrainer(
                trainer.getId(),
                List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED)
        ).stream().anyMatch(p -> p.getId().equals(userId));

        if (!allowed) {
            ra.addFlashAttribute("noteError", "You cannot add notes for this user.");
            return "redirect:/trainer";
        }

        TrainerNote note = new TrainerNote();
        note.setTrainer(trainer);
        note.setUser(user);
        note.setContent(content);

        trainerNoteRepository.save(note);

        ra.addFlashAttribute("noteMessage", "Note saved!");
        return "redirect:/trainer?userId=" + userId;
    }
}


