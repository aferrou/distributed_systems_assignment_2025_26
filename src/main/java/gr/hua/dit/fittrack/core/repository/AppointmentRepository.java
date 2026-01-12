package gr.hua.dit.fittrack.core.repository;

import gr.hua.dit.fittrack.core.model.Appointment;

import gr.hua.dit.fittrack.core.model.AppointmentStatus;

import gr.hua.dit.fittrack.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Repository for {@link Appointment} entity.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
        select distinct a.user
        from Appointment a
        where a.trainer.id = :trainerId
          and a.status in :statuses
        order by a.user.lastName, a.user.firstName
    """)
    List<Person> findDistinctUsersForTrainer(@Param("trainerId") Long trainerId,
                                             @Param("statuses") List<AppointmentStatus> statuses);

    List<Appointment> findAllByUserId(long userId);

    List<Appointment> findAllByTrainerId(long trainerId);


    List<Appointment> findByStatusAndCreatedAtBefore(
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

    /**
     * Find active appointments for a user that have a scheduled time.
     */
    List<Appointment> findByUserIdAndStatusInAndScheduledAtIsNotNull(
            long userId,
            Set<AppointmentStatus> statuses
    );

    /**
     * Find active appointments for a trainer that have a scheduled time.
     */
    List<Appointment> findByTrainerIdAndStatusInAndScheduledAtIsNotNull(
            long trainerId,
            Set<AppointmentStatus> statuses
    );
}