package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.model.Client;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.ClientRepository;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import gr.hua.dit.fittrack.core.service.model.CreatePersonRequest;

import gr.hua.dit.fittrack.core.service.model.CreatePersonResult;
import jakarta.annotation.PostConstruct;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes application.
 */
@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final ClientRepository clientRepository;
    private final PersonRepository personRepository;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final PasswordEncoder passwordEncoder;
    private final AtomicBoolean initialized;

    public InitializationService(final ClientRepository clientRepository,
                                 final PersonRepository personRepository,
                                 final PersonBusinessLogicService personBusinessLogicService,
                                 final PasswordEncoder passwordEncoder) {
        if (clientRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();

        this.clientRepository = clientRepository;
        this.personRepository = personRepository;
        this.personBusinessLogicService = personBusinessLogicService;
        this.passwordEncoder = passwordEncoder;
        this.initialized = new AtomicBoolean(false);
    }

    @PostConstruct
    @Transactional
    public void populateDatabaseWithInitialData() {
        final boolean alreadyInitialized = this.initialized.getAndSet(true);
        if (alreadyInitialized) {
            LOGGER.warn("Database initialization skipped: initial data has already been populated.");
            return;
        }

        LOGGER.info("Starting database initialization with initial data...");

        // Initialize OAuth2 clients for API access
        // --------------------------------------------------
        initializeClients();

        // Initialize admin account
        // --------------------------------------------------
        // initializeAdmin();

        // Initialize test trainers and users
        // --------------------------------------------------
        try {
            initializeTestAccounts();
        } catch (Exception e) {
            LOGGER.warn("Could not create test accounts (external service may be unavailable): {}", e.getMessage());
            LOGGER.info("You can register new accounts via the web UI at /register");
        }

        LOGGER.info("Database initialization completed successfully.");
    }

    private void initializeClients() {
        if (clientRepository.count() > 0) {
            LOGGER.info("Clients already exist, skipping client initialization.");
            return;
        }

        final List<Client> clientList = List.of(
                new Client(null, "client01", "s3cr3t", "INTEGRATION_READ,INTEGRATION_WRITE"),
                new Client(null, "client02", "s3cr3t", "INTEGRATION_READ")
        );

        clientRepository.saveAll(clientList);
        LOGGER.info("Created {} OAuth2 clients", clientList.size());
    }

//    private void initializeAdmin() {
//        if (personRepository.existsByUsernameIgnoreCase("admin")) {
//            LOGGER.info("Admin account already exists, skipping admin initialization.");
//            return;
//        }
//
//        // Create admin directly via repository to bypass security checks
//        final var admin = new gr.hua.dit.fittrack.core.model.Person();
//        admin.setUsername("admin");
//        admin.setType(PersonType.ADMIN);
//        admin.setFirstName("System");
//        admin.setLastName("Administrator");
//        admin.setEmailAddress("admin@fittrack.com");
//        admin.setMobilePhoneNumber("+306900000000");
//        admin.setPasswordHash(passwordEncoder.encode("admin123"));
//
//        personRepository.save(admin);
//        LOGGER.info("✅ Created admin account: username=admin, password=admin123");
//    }

    private void initializeTestAccounts() {
        if (personRepository.count() > 0) {
            LOGGER.info("Test accounts already exist, skipping test account initialization.");
            return;
        }

        final List<CreatePersonRequest> createPersonRequestList = List.of(
                // Trainers - Greek trainers with specializations
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "giannis.papadopoulos",
                        "Γιάννης",
                        "Παπαδόπουλος",
                        "giannis.papadopoulos@fittrack.gr",
                        "trainer123",
                        "Προπόνηση Δύναμης",
                        "Αθήνα"
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "maria.konstantinou",
                        "Μαρία",
                        "Κωνσταντίνου",
                        "maria.konstantinou@fittrack.gr",
                        "trainer123",
                        "Yoga & Pilates",
                        "Θεσσαλονίκη"
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "nikos.athanassiou",
                        "Νίκος",
                        "Αθανασίου",
                        "nikos.athanassiou@fittrack.gr",
                        "trainer123",
                        "Crossfit & Outdoor Training",
                        "Πάτρα"
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "eleni.dimitriou",
                        "Ελένη",
                        "Δημητρίου",
                        "eleni.dimitriou@fittrack.gr",
                        "trainer123",
                        "Functional & Outdoor Training",
                        "Αθήνα"
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "kostas.georgiou",
                        "Κώστας",
                        "Γεωργίου",
                        "kostas.georgiou@fittrack.gr",
                        "trainer123",
                        "Bodybuilding",
                        "Ηράκλειο"
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "sofia.panagioutou",
                        "Σοφία",
                        "Παναγιώτου",
                        "sofia.panagioutou@fittrack.gr",
                        "trainer123",
                        "Cardio & Outdoor Training",
                        "Θεσσαλονίκη"
                ),
                // Users
                new CreatePersonRequest(
                        PersonType.USER,
                        "giorgos.karagiannis",
                        "Γιώργος",
                        "Καραγιάννης",
                        "giorgos.karagiannis@email.gr",
                        "user123",
                        null,
                        null
                ),
                new CreatePersonRequest(
                        PersonType.USER,
                        "katerina.papadopoulou",
                        "Κατερίνα",
                        "Παπαδοπούλου",
                        "katerina.papadopoulou@email.gr",
                        "user123",
                        null,
                        null
                ),
                new CreatePersonRequest(
                        PersonType.USER,
                        "dimitris.nikolaou",
                        "Δημήτρης",
                        "Νικολάου",
                        "dimitris.nikolaou@email.gr",
                        "user123",
                        null,
                        null
                )
        );

        int successCount = 0;
        int failCount = 0;

        for (final CreatePersonRequest request : createPersonRequestList) {
            final CreatePersonResult result = personBusinessLogicService.createPerson(request, false);

            if (result.success()) {
                successCount++;
                LOGGER.info("✅ Created {} account: {} {} ({})", request.type(), request.firstName(), request.lastName(), request.emailAddress());
            } else {
                failCount++;
                LOGGER.error("❌ Failed to create {} account {}: {}",
                        request.type(), request.emailAddress(), result.errorMessage());
            }
        }

        LOGGER.info("Test account creation completed: {} successful, {} failed", successCount, failCount);
    }
}