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

    Optional<Person> findByUsername(final String username);

    Optional<Person> findByEmailAddressIgnoreCase(final String emailAddress);

    List<Person> findAllByTypeOrderByLastName(final PersonType type);

    boolean existsByEmailAddressIgnoreCase(final String emailAddress);

    boolean existsByMobilePhoneNumber(final String mobilePhoneNumber);

    boolean existsByUsernameIgnoreCase(final String username);
}