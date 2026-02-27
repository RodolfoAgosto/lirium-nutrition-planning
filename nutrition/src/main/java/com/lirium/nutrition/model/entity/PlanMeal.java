package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.MealType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a meal within a DailyPlan, e.g., breakfast, lunch, or dinner.
 * Each PlanMeal contains a list of PlanFoodPortion instances,
 * forming the meal's composition. This entity is part of the DailyPlan aggregate,
 * and food portions should be added via `addFoodPortion` to maintain consistency.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PlanMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MealType type;

    @OneToMany(mappedBy = "meal", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PlanFoodPortion> foods = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id")
    private DailyPlan dailyPlan;

    private PlanMeal(MealType mealType, DailyPlan dailyPlan){
        Objects.requireNonNull(mealType, "Must to specify a meal type.");
        Objects.requireNonNull(dailyPlan, "Must to specify a Daily Plan.");
        this.type = mealType;
        this.dailyPlan = dailyPlan;
    }

    public static PlanMeal of(MealType mealType, DailyPlan dailyPlan){
        return new PlanMeal(mealType, dailyPlan);
    }

    public void addFoodPortion(PlanFoodPortion planFoodPortion) {

        Objects.requireNonNull(planFoodPortion, "The food portion cannot be null");
        if (foods.contains(planFoodPortion)) return;
        foods.add(planFoodPortion);

    }

    public void removeFoodPortion(PlanFoodPortion planFoodPortion) {

        Objects.requireNonNull(planFoodPortion);
        foods.remove(planFoodPortion);

    }

    public void clearFoods() {
        foods.clear();
    }

    public List<PlanFoodPortion> getFoodPortions() {
        return Collections.unmodifiableList(foods);
    }

    void assignToPlan(DailyPlan plan) {
        this.dailyPlan = plan;
    }

}