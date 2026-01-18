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

    private void initializeTestAccounts() {
        if (personRepository.count() > 0) {
            LOGGER.info("Test accounts already exist, skipping test account initialization.");
            return;
        }

        final List<CreatePersonRequest> createPersonRequestList = List.of(
                // Trainers - Greek trainers with specializations
                // Working days: 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "giannis.papadopoulos",
                        "Γιάννης",
                        "Παπαδόπουλος",
                        "giannis.papadopoulos@fittrack.gr",
                        "trainer123T!",
                        "Προπόνηση Δύναμης",
                        "Αθήνα",
                        "1,2,4,5" // Monday, Tuesday, Thursday, Friday
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "maria.konstantinou",
                        "Μαρία",
                        "Κωνσταντίνου",
                        "maria.konstantinou@fittrack.gr",
                        "trainer123T!",
                        "Yoga & Pilates",
                        "Θεσσαλονίκη",
                        "1,3,4,6" // Monday, Wednesday, Thursday, Saturday
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "nikos.athanassiou",
                        "Νίκος",
                        "Αθανασίου",
                        "nikos.athanassiou@fittrack.gr",
                        "trainer123T!",
                        "Crossfit & Outdoor Training",
                        "Πάτρα",
                        "2,3,5,6" // Tuesday, Wednesday, Friday, Saturday
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "eleni.dimitriou",
                        "Ελένη",
                        "Δημητρίου",
                        "eleni.dimitriou@fittrack.gr",
                        "trainer123T!",
                        "Functional & Outdoor Training",
                        "Αθήνα",
                        "1,2,4,6" // Monday, Tuesday, Thursday, Saturday
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "kostas.georgiou",
                        "Κώστας",
                        "Γεωργίου",
                        "kostas.georgiou@fittrack.gr",
                        "trainer123T!",
                        "Bodybuilding",
                        "Ηράκλειο",
                        "3,4,5,6" // Wednesday, Thursday, Friday, Saturday
                ),
                new CreatePersonRequest(
                        PersonType.TRAINER,
                        "sofia.panagioutou",
                        "Σοφία",
                        "Παναγιώτου",
                        "sofia.panagioutou@fittrack.gr",
                        "trainer123T!",
                        "Cardio & Outdoor Training",
                        "Θεσσαλονίκη",
                        "1,2,3,4" // Monday, Tuesday, Wednesday, Thursday
                ),
                // Users
                new CreatePersonRequest(
                        PersonType.USER,
                        "giorgos.karagiannis",
                        "Γιώργος",
                        "Καραγιάννης",
                        "giorgos.karagiannis@email.gr",
                        "user123U!",
                        null,
                        null,
                        null // users don't have working days
                ),
                new CreatePersonRequest(
                        PersonType.USER,
                        "katerina.papadopoulou",
                        "Κατερίνα",
                        "Παπαδοπούλου",
                        "katerina.papadopoulou@email.gr",
                        "user123U!",
                        null,
                        null,
                        null // users don't have working days
                ),
                new CreatePersonRequest(
                        PersonType.USER,
                        "dimitris.nikolaou",
                        "Δημήτρης",
                        "Νικολάου",
                        "dimitris.nikolaou@email.gr",
                        "user123U!",
                        null,
                        null,
                        null // users don't have working days
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