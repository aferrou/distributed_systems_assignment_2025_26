package gr.hua.dit.fittrack.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import jakarta.persistence.UniqueConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Person entity.
 */
@Entity
@Table(
        name = "person",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_person_email_address", columnNames = "email_address")
        },
        indexes = {
                @Index(name = "idx_person_type", columnList = "type"),
                @Index(name = "idx_person_last_name", columnList = "last_name")
        }
)
public final class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PersonType type;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 20)
    @Column(name = "mobile_phone_number", length = 20)
    private String mobilePhoneNumber;

    @Size(max = 100)
    @Column(name = "specialisation", length = 100)
    private String specialisation;

    @Size(max = 100)
    @Column(name = "training_area", length = 100)
    private String trainArea;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Person() {
    }

    public Person(Long id,
                  String username,
                  String firstName,
                  String lastName,
                  String emailAddress,
                  PersonType type,
                  String passwordHash,
                  String mobilePhoneNumber,
                  String specialisation,
                  String trainArea,
                  Instant createdAt) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.type = type;
        this.passwordHash = passwordHash;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.specialisation = specialisation;
        this.trainArea = trainArea;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public PersonType getType() {
        return type;
    }

    public void setType(PersonType type) {
        this.type = type;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getSpecialisation() {
        return specialisation;
    }

    public void setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
    }

    public String getTrainArea() {
        return trainArea;
    }

    public void setTrainArea(String trainArea) {
        this.trainArea = trainArea;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username=" + username +
                ", type=" + type +
                '}';
    }
}