package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.service.PersonBusinessLogicService;
import gr.hua.dit.fittrack.core.service.mapper.PersonMapper;
import gr.hua.dit.fittrack.core.service.model.CreatePersonRequest;
import gr.hua.dit.fittrack.core.service.model.CreatePersonResult;
import gr.hua.dit.fittrack.core.service.model.PersonView;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link PersonBusinessLogicService}.
 */
@Service
public class PersonBusinessLogicServiceImpl implements PersonBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonBusinessLogicServiceImpl.class);

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonBusinessLogicServiceImpl(final Validator validator,
                                          final PasswordEncoder passwordEncoder,
                                          final PersonRepository personRepository,
                                          final PersonMapper personMapper) {
        if (validator == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();

        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @Transactional
    @Override
    public CreatePersonResult createTrainer(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        // SECURITY: Force TRAINER type
        // --------------------------------------------------
        if (createPersonRequest.type() != PersonType.TRAINER) {
            return CreatePersonResult.fail("This method only creates TRAINER accounts");
        }

        // Reuse the existing createPerson logic
        // --------------------------------------------------
        return this.createPerson(createPersonRequest, notify);
    }

    @Transactional
    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        // `CreatePersonRequest` validation.
        // --------------------------------------------------

        final Set<ConstraintViolation<CreatePersonRequest>> requestViolations
                = this.validator.validate(createPersonRequest);
        if (!requestViolations.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final ConstraintViolation<CreatePersonRequest> violation : requestViolations) {
                sb
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("\n");
            }
            return CreatePersonResult.fail(sb.toString());
        }

        // Unpack (we assume valid `CreatePersonRequest` instance)
        // --------------------------------------------------

        final PersonType type = createPersonRequest.type();
        final String username = createPersonRequest.username().strip(); // remove whitespaces
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress = createPersonRequest.emailAddress().strip();
        final String rawPassword = createPersonRequest.rawPassword();
        final String specialisation = createPersonRequest.specialisation();
        final String trainArea = createPersonRequest.trainArea();

        // Basic email address validation.
        // --------------------------------------------------

        final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress);
        if (!matcher.matches()) {
            return CreatePersonResult.fail("Email address is not valid");
        }

        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)) {
            return CreatePersonResult.fail("Email Address already registered");
        }

        // --------------------------------------------------

        final String hashedPassword = this.passwordEncoder.encode(rawPassword);

        // Instantiate person.
        // --------------------------------------------------

        Person person = new Person();
        person.setId(null); // auto generated
        person.setUsername(username);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null); // auto generated.

        // Set trainer-specific fields
        if (type == PersonType.TRAINER) {
            person.setSpecialisation(specialisation);
            person.setTrainArea(trainArea);
        }

        // --------------------------------------------------

        final Set<ConstraintViolation<Person>> personViolations = this.validator.validate(person);
        if (!personViolations.isEmpty()) {
            // Throw an exception instead of returning an instance, i.e. `CreatePersonResult.fail`.
            // At this point, errors/violations on the `Person` instance
            // indicate a programmer error, not a client error.
            throw new RuntimeException("invalid Person instance");
        }

        // Persist person (save/insert to database)
        // --------------------------------------------------

        person = this.personRepository.save(person);

        // Map `Person` to `PersonView`.
        // --------------------------------------------------

        final PersonView personView = this.personMapper.convertPersonToPersonView(person);

        // --------------------------------------------------

        return CreatePersonResult.success(personView);
    }
}
