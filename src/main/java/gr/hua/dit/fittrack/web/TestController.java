package gr.hua.dit.fittrack.web;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for testing.
 */
@RestController
public class TestController {

    private final PersonRepository personRepository;

    public TestController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * REST endpoint for testing.
     */
    @GetMapping(value = "/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test() {
        // Example 1: create Person.
        Person person = new Person();
        person.setId(null); // auto-generated
        person.setUsername("member01");
        person.setType(PersonType.USER);
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmailAddress("john.doe@gmail.com");
        person.setPasswordHash("<invalid>");
        person.setCreatedAt(Instant.now()); // just now

        person = this.personRepository.save(person);

        return person.toString();
    }
}
