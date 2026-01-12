package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.TrainerNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerNoteRepository extends JpaRepository<TrainerNote, Long> {

    List<TrainerNote> findByTrainer_IdAndUser_IdOrderByCreatedAtDesc(Long trainerId, Long userId);

}
