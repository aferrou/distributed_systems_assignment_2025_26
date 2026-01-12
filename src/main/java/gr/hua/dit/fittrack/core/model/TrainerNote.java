package gr.hua.dit.fittrack.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TrainerNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private Person trainer;

    @ManyToOne(optional=false)
    private Person user;

    @Column(nullable=false, length = 2000)
    private String content;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TrainerNote(Long id, Person trainer, Person user, String content, LocalDateTime createdAt) {
        this.id = id;
        this.trainer = trainer;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
    }
    public TrainerNote() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getTrainer() {
        return trainer;
    }

    public void setTrainer(Person trainer) {
        this.trainer = trainer;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
