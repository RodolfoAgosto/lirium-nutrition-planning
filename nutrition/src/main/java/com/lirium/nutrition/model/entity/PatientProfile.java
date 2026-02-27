package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a patient's profile with personal, anthropometric, and medical data.
 * Contains sex, activity level, weight, height, medical notes, dietary restrictions,
 * physiological conditions, and primary nutrition goal.
 * Acts as the aggregate root for patient-specific nutrition information.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name="user_id", unique = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel activityLevel;

    private BigDecimal weight;

    private Integer height;

    @Column(columnDefinition = "TEXT")
    private String medicalNotes;

    @ManyToMany
    @JoinTable(
            name = "patient_profile_restriction",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "restriction_id")
    )
    private Set<Restriction> restrictions = new HashSet<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<PhysiologicalCondition> physiologicalConditions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GoalType primaryGoal;

    public void addRestriction(Restriction restriction) {
        Objects.requireNonNull(restriction);
        restrictions.add(restriction);
    }

    public PatientProfile(User user) {
        this.user = Objects.requireNonNull(user);
    }

}