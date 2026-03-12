package com.lirium.nutrition.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter @Setter
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String passwordHash;

    @Column(length = 60)
    private String firstName;

    @Column(length = 60)
    private String lastName;

    private LocalDate birthDate;

    @Column(unique = true, length = 20)
    private String dni;

    private Boolean emailValidated = false;

    @Builder.Default
    private boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PatientProfile patientProfile;

    public User() {
        this.patientProfile = new PatientProfile(this);
    }

    public User(String email, String passwordHash, String firstName, String lastName) {
        this();
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.firstName = firstName;
        this.lastName = lastName;
    }

}