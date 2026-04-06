package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a nutrition plan with macro targets and weekly meal structure.
 * Defines dietary goals for a given period.
 */

@Entity
@Table(name = "nutrition_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NutritionPlan {

    @Id
    @SequenceGenerator(
            name = "nutrition_plan_seq",
            sequenceName = "nutrition_plan_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nutrition_plan_seq")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private GoalType targetGoal;

    private int dailyCalories;

    private int proteinGrams;

    private int carbGrams;

    private int fatGrams;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id")
    private PatientProfile patientProfile;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyPlan> week = new ArrayList<>();

    // The system generates the minimum required fields
    public static NutritionPlan generate(
            GoalType targetGoal,
            int dailyCalories,
            int proteinGrams,
            int carbGrams,
            int fatGrams,
            PatientProfile patient) {

        NutritionPlan plan = new NutritionPlan();
        plan.targetGoal = targetGoal;
        plan.dailyCalories = dailyCalories;
        plan.proteinGrams = proteinGrams;
        plan.carbGrams = carbGrams;
        plan.fatGrams = fatGrams;
        plan.patientProfile = patient;
        plan.status = PlanStatus.DRAFT;  // Always starts as a draft
        return plan;
    }

    // The nutritionist completes the draft
    public void completeBasic(String name, String description) {

        requireText(name, "Name is required");
        requireText(description, "Description is required");

        if (this.status != PlanStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT plans can be completed");

        this.name = name;
        this.description = description;
    }

    public void addDailyPlan(DailyPlan dailyPlan) {
        Objects.requireNonNull(dailyPlan);
        this.week.add(dailyPlan);
    }

    private static void requireText(String s, String msg) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(msg);
        }
    }

    public void update(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            GoalType targetGoal,
            Integer dailyCalories,
            Integer proteinGrams,
            Integer carbGrams,
            Integer fatGrams
    ) {

        if(name!=null) requireText(name,"Name required");
        if(description!=null) requireText(description,"Description required");

        LocalDate newStart = startDate!=null ? startDate : this.startDate;
        LocalDate newEnd   = endDate!=null ? endDate : this.endDate;

        if(newEnd!=null && newStart!=null && newEnd.isBefore(newStart))
            throw new IllegalArgumentException("End date before start date");

        if(dailyCalories!=null && dailyCalories<=0)
            throw new IllegalArgumentException("Daily calories must be greater than zero");

        if(proteinGrams!=null && proteinGrams<0)
            throw new IllegalArgumentException("Protein grams cannot be negative");
        if(carbGrams!=null && carbGrams<0)
            throw new IllegalArgumentException("Carb grams cannot be negative");
        if(fatGrams!=null && fatGrams<0)
            throw new IllegalArgumentException("Fat grams cannot be negative");

        if(name!=null) this.name=name;
        if(description!=null) this.description=description;
        if(startDate!=null) this.startDate=startDate;
        if(endDate!=null) this.endDate=endDate;
        if(targetGoal!=null) this.targetGoal=targetGoal;
        if(dailyCalories!=null) this.dailyCalories=dailyCalories;
        if(proteinGrams!=null) this.proteinGrams=proteinGrams;
        if(carbGrams!=null) this.carbGrams=carbGrams;
        if(fatGrams!=null) this.fatGrams=fatGrams;
    }

    public boolean isDraft() {
        return status == PlanStatus.DRAFT;
    }

    public boolean isActive() {
        return status == PlanStatus.ACTIVE;
    }

    public void activate(LocalDate startDate) {

        Objects.requireNonNull(startDate, "Start date is required");

        if (status != PlanStatus.DRAFT)
            throw new IllegalStateException(
                    "Only DRAFT plans can be activated. Current status: " + status);

        this.status = PlanStatus.ACTIVE;
        this.startDate = startDate;
    }


    public void deactivate() {
        if (status != PlanStatus.ACTIVE)
            throw new IllegalStateException(
                    "Only ACTIVE plans can be deactivated. Current status: " + status);
        this.status = PlanStatus.INACTIVE;
    }

    public void close(LocalDate endDate) {
        Objects.requireNonNull(endDate, "End date required");
        if (status != PlanStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE plans can be closed");
        this.endDate = endDate;
        this.status = PlanStatus.INACTIVE;
    }

}
