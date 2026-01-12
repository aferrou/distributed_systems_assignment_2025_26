package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.Availability;
import gr.hua.dit.fittrack.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByTrainer(Person trainer);
}
