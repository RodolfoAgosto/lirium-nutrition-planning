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

}
