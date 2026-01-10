package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.Appointment;

import gr.hua.dit.fittrack.core.model.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Repository for {@link Appointment} entity.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByUserId(long userId);

    List<Appointment> findAllByTrainerId(long trainerId);

    List<Appointment> findByStatusAndRequestedAtBefore(
            AppointmentStatus status,
            Instant before
    );

    boolean existsByUserIdAndTrainerIdAndStatusIn(
            long userId,
            long trainerId,
            Set<AppointmentStatus> statuses
    );

    boolean existsByTrainerIdAndScheduledAt(
            long trainerId,
            Instant scheduledAt
    );

    long countByUserIdAndStatusIn(
            long userId,
            Set<AppointmentStatus> statuses
    );
}