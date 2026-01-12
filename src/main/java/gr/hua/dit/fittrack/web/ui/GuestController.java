package gr.hua.dit.fittrack.web.ui;

import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.PersonView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class GuestController {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public GuestController(PersonRepository personRepository, PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @GetMapping("/guest")
    public String showGuestPage(Model model) {
        List<PersonView> trainers = personRepository
                .findAllByTypeOrderByLastName(PersonType.TRAINER)
                .stream()
                .map(personMapper::convertPersonToPersonView)
                .toList();

        model.addAttribute("trainers", trainers);
        return "guest";
    }
}
