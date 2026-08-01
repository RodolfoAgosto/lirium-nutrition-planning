package com.lirium.nutrition.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a day within a NutritionPlan aggregate.
 * A DailyPlan groups the meals planned for a specific DayOfWeek and belongs to exactly one NutritionPlan.
 * Encapsulation rules:
 * - Meals are managed through add/remove methods only.
 * - Collections are exposed as read-only views.
 * - A DailyPlan cannot exist without a NutritionPlan.
 */

@Entity
@Table(name = "daily_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class DailyPlan {

    @Id
    @SequenceGenerator(
            name = "daily_plan_seq",
            sequenceName = "daily_plan_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "daily_plan_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @OneToMany(
            mappedBy = "dailyPlan",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<PlanMeal> meals = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    private NutritionPlan nutritionPlan;

    private DailyPlan(DayOfWeek day, NutritionPlan nutritionPlan) {
        Objects.requireNonNull(day, "Day cannot be null");
        Objects.requireNonNull(nutritionPlan, "Nutrition Plan cannot be null");
        this.nutritionPlan = nutritionPlan;
        this.dayOfWeek = day;
    }

    public static DailyPlan of(DayOfWeek day, NutritionPlan nutritionPlan) {
        return new DailyPlan(day, nutritionPlan);
    }

    public PlanMeal addMeal(PlanMeal planMeal) {
        Objects.requireNonNull(planMeal, "PlanMeal cannot be null");

        boolean alreadyExists = meals.stream()
                .anyMatch(m -> m.getType() == planMeal.getType());

        if (alreadyExists)
            throw new IllegalArgumentException(
                    "A meal of type " + planMeal.getType() + " already exists for this day");

        meals.add(planMeal);
        return planMeal;
    }

    public void removeMeal(PlanMeal meal) {

        Objects.requireNonNull(meal);
        if (meals.remove(meal)) {
            meal.assignToPlan(null);
        }

    }

    public void clearMeals() {
        for (PlanMeal meal : meals) {
            meal.assignToPlan(null);
        }
        meals.clear();
    }

    public List<PlanMeal> getMeals() {
        return Collections.unmodifiableList(meals);
    }

}