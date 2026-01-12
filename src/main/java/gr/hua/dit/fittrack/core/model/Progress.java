package gr.hua.dit.fittrack.core.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "workout_duration")
    private Integer workoutDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type")
    private TrainingType trainingType;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Constructors
    public Progress() {
    }

    public Progress(Long userId, LocalDate date, Double weight, Integer workoutDuration, TrainingType trainingType, String notes) {
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.workoutDuration = workoutDuration;
        this.trainingType = trainingType;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getWorkoutDuration() {
        return workoutDuration;
    }

    public void setWorkoutDuration(Integer workoutDuration) {
        this.workoutDuration = workoutDuration;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
