package gr.hua.dit.fittrack.web.ui;

import org.springframework.ui.Model;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.service.PersonBusinessLogicService;
import gr.hua.dit.fittrack.core.service.model.CreatePersonRequest;
import gr.hua.dit.fittrack.core.service.model.CreatePersonResult;
import gr.hua.dit.fittrack.web.ui.model.CreatePersonForm;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/trainers")
@PreAuthorize("hasRole('ADMIN')")  // ← ONLY ADMINS
public class AdminTrainerController {

    private final PersonBusinessLogicService personService;

    public AdminTrainerController(final PersonBusinessLogicService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }

    /**
     * Show form to create a new trainer
     */
    @GetMapping("/new")
    public String showCreateTrainerForm(final Model model) {
        model.addAttribute("form", new CreatePersonForm(
                "", "", "", "", ""
        ));
        return "admin/trainer_new";
    }

    /**
     * Handle trainer creation
     */
    @PostMapping("/new")
    public String handleCreateTrainer(
            @ModelAttribute("form") @Valid final CreatePersonForm form,
            final BindingResult bindingResult,
            final Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/trainer_new";
        }

        // SECURITY: Force PersonType.TRAINER
        final CreatePersonRequest request = new CreatePersonRequest(
                PersonType.TRAINER,
                form.username(),
                form.firstName(),
                form.lastName(),
                form.email(),
                form.rawPassword(),
                null,  // specialisation - not provided in this form
                null,  // trainArea - not provided in this form
                null   // workingDays - not provided in this form
        );

        final CreatePersonResult result = personService.createTrainer(request, true);

        if (!result.success()) {
            model.addAttribute("error", result.errorMessage());
            return "admin/trainer_new";
        }

        return "redirect:/admin/trainers?created";
    }

//    /**
//     * List all trainers (admin view)
//     */
//    @GetMapping
//    public String listTrainers(final Model model) {
//        // TODO: Implement PersonBusinessLogicService.getAllTrainers()
//        // List<PersonView> trainers = personService.getAllTrainers();
//        // model.addAttribute("trainers", trainers);
//        return "admin/trainers";
//    }
}
