package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Person} entity.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    // Find a person by username
    Optional<Person> findByUsername(final String username);

    // Find a person by email
    Optional<Person> findByEmailAddressIgnoreCase(final String emailAddress);

    // Find all persons of a specific type
    List<Person> findAllByTypeOrderByLastName(final PersonType type);

    // Existence checks for validation
    boolean existsByEmailAddressIgnoreCase(final String emailAddress);

    boolean existsByUsernameIgnoreCase(final String username);
}
