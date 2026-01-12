package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUserIdOrderByDateDesc(Long userId);
}
