package gr.hua.dit.fittrack.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Appointment entity.
 */
@SuppressWarnings("JpaDataSourceORMInspection") // Το σβήνω στο τέλος
@Entity
@Table(
        name = "appointment",
        indexes = {
                @Index(name = "idx_appointment_status", columnList = "status"),
                @Index(name = "idx_appointment_user", columnList = "user_id"),
                @Index(name = "idx_appointment_trainer", columnList = "trainer_id"),
                @Index(name = "idx_appointment_scheduled_at", columnList = "scheduled_at")
        }
)
public final class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appointment_user"))
    private Person user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appointment_trainer"))
    private Person trainer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AppointmentStatus status;

    @NotNull
    @Column(name = "training_type", nullable = false)
    private TrainingType trainingType;

    /**
     * When the actual training session is scheduled to happen.
     */
    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    /**
     * Optional notes provided by the user.
     */
    @Size(max = 1000)
    @Column(name = "user_notes", length = 1000)
    private String userNotes;

    /**
     * Optional notes provided by the trainer.
     */
    @Size(max = 1000)
    @Column(name = "trainer_notes", length = 1000)
    private String trainerNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Coordinates for outdoor training location.
     */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public Appointment() {
        // JPA
    }

    public Appointment(Long id,
                       Person user,
                       Person trainer,
                       TrainingType trainingType,
                       AppointmentStatus status,
                       Instant scheduledAt,
                       String userNotes,
                       String trainerNotes,
                       Instant createdAt,
                       Instant confirmedAt,
                       Instant completedAt) {
        this.id = id;
        this.user = user;
        this.trainer = trainer;
        this.trainingType = trainingType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.userNotes = userNotes;
        this.trainerNotes = trainerNotes;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public Person getTrainer() {
        return trainer;
    }

    public void setTrainer(Person trainer) {
        this.trainer = trainer;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public String getTrainerNotes() {
        return trainerNotes;
    }

    public void setTrainerNotes(String trainerNotes) {
        this.trainerNotes = trainerNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", user=" + user +
                ", trainer=" + trainer +
                ", status=" + status +
                ", scheduledAt=" + scheduledAt +
                '}';
    }
}
