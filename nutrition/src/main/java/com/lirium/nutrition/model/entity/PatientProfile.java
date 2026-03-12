package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.*;
import jakarta.persistence.*;
import lombok.*;

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
    private Sex sex;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Embedded
    private Weight weight;

    @Embedded
    private Height height;

    @Column(columnDefinition = "TEXT")
    private String medicalNotes;

    @ManyToMany
    @JoinTable(
            name = "patient_profile_restriction",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "restriction_id")
    )
    private Set<Restriction> restrictions = new HashSet<>();

    public Set<Restriction> getRestrictions() {
        return Collections.unmodifiableSet(restrictions);
    }

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<PhysiologicalCondition> physiologicalConditions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private GoalType primaryGoal;

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL)
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();

    public PatientProfile(User user) {
        this.user = Objects.requireNonNull(user);
    }

    public void addRestriction(Restriction restriction) {
        Objects.requireNonNull(restriction);
        restrictions.add(restriction);
    }

    public void update(
            Sex sex,
            ActivityLevel activityLevel,
            Weight weight,
            Height height,
            String medicalNotes,
            Set<Restriction> restrictions,
            List<PhysiologicalCondition> conditions,
            GoalType primaryGoal
    ) {

        if (sex != null) this.sex = sex;

        if (activityLevel != null) this.activityLevel = activityLevel;

        if (weight != null) this.weight = weight;

        if (height != null) this.height = height;

        if (medicalNotes != null) this.medicalNotes = medicalNotes;

        if (primaryGoal != null) this.primaryGoal = primaryGoal;

        if (restrictions != null) {
            this.restrictions.clear();
            this.restrictions.addAll(restrictions);
        }

        if (conditions != null) {
            this.physiologicalConditions.clear();
            this.physiologicalConditions.addAll(conditions);
        }
    }

    public void updateNutritionProfile(
            Height height,
            Weight weight,
            ActivityLevel activityLevel,
            GoalType goal
    ) {

        if (height != null) this.height = height;

        if (weight != null) this.weight = weight;

        if (activityLevel != null) this.activityLevel = activityLevel;

        if (goal != null) this.primaryGoal = goal;
    }

}