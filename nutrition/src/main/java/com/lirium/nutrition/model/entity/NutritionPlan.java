package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.GoalType;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NutritionPlan {

    @Id
    @SequenceGenerator(
            name = "nutrition_plan_seq",
            sequenceName = "nutrition_plan_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nutrition_plan_seq")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

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

    public static NutritionPlan of(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            GoalType targetGoal,
            int dailyCalories,
            int proteinGrams,
            int carbGrams,
            int fatGrams
    ) {

        requireText(name, "Name is required");
        requireText(description, "Description is required");

        Objects.requireNonNull(startDate, "Start date is required");
        Objects.requireNonNull(endDate, "End date is required");
        Objects.requireNonNull(targetGoal, "Goal type is required");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if (dailyCalories <= 0) {
            throw new IllegalArgumentException("Daily calories must be greater than zero");
        }

        if (proteinGrams < 0 || carbGrams < 0 || fatGrams < 0) {
            throw new IllegalArgumentException("Macro grams cannot be negative");
        }

        NutritionPlan plan = new NutritionPlan();
        plan.name = name;
        plan.description = description;
        plan.startDate = startDate;
        plan.endDate = endDate;
        plan.targetGoal = targetGoal;
        plan.dailyCalories = dailyCalories;
        plan.proteinGrams = proteinGrams;
        plan.carbGrams = carbGrams;
        plan.fatGrams = fatGrams;

        return plan;
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

}
