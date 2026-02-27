package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.valueobject.Grams;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.util.Objects;

/**
 * Represents a portion of food within a specific plan meal.
 * This entity stores the reference to a Food, the quantity (in grams),
 * and belongs to a PlanMeal.
 */

@Entity
@Table(name = "plan_food_portions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PlanFoodPortion extends AbstractFoodPortion{

    @Id
    @SequenceGenerator(
            name = "plan_food_portion_seq",
            sequenceName = "plan_food_portion_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_food_portion_seq")
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private PlanMeal meal;

    private PlanFoodPortion(PlanMeal meal, Food food, Grams grams) {
        super(Objects.requireNonNull(food, "Food cannot be null"),
                Objects.requireNonNull(grams, "Grams cannot be null"));
        this.meal = Objects.requireNonNull(meal, "Meal cannot be null");
    }

    public static PlanFoodPortion of(PlanMeal mealPlan, Food food, Grams grams){
        return new PlanFoodPortion(mealPlan, food, grams);
    }

}