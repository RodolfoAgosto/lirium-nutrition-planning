package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Historical snapshot of a patient's profile at a given visit date.
 * Stores weight, height, medical notes, dietary restrictions, physiological conditions,
 * and primary nutrition goal at that moment.
 * Linked to a specific PatientProfile and acts as an audit/history record.
 */

@Entity
@Table(name = "plan_food_portions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PatientProfileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    private LocalDate visitDate;

    private BigDecimal weight;

    private Integer height;

    @Column(columnDefinition = "TEXT")
    private String medicalNotes;

    @ManyToMany
    @JoinTable(
            name = "patient_profile_history_restriction",
            joinColumns = @JoinColumn(name = "history_id"),
            inverseJoinColumns = @JoinColumn(name = "restriction_id")
    )
    private Set<Restriction> restrictions = new HashSet<>();

    @ElementCollection(targetClass = PhysiologicalCondition.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "patient_history_conditions",
            joinColumns = @JoinColumn(name = "patient_history_id")
    )
    @Column(name = "condition")
    private Set<PhysiologicalCondition> physiologicalConditions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private GoalType primaryGoal;

    public PatientProfileHistory(PatientProfile patientProfile) {
        this.patientProfile = Objects.requireNonNull(patientProfile);
        this.visitDate = LocalDate.now();
        this.weight = patientProfile.getWeight();
        this.height = patientProfile.getHeight();
        this.medicalNotes = patientProfile.getMedicalNotes();
        this.primaryGoal = patientProfile.getPrimaryGoal();
        this.restrictions = new HashSet<>(patientProfile.getRestrictions());
        this.physiologicalConditions = new HashSet<>(patientProfile.getPhysiologicalConditions());
    }

}