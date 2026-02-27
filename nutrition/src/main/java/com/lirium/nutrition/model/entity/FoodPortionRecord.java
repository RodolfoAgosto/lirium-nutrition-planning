package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.valueobject.Grams;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing a portion of a Food within a MealRecord}.
 * Belongs to the MealRecord aggregate and cannot exist without it.
 * Quantity is expressed in Grams.
 */

@Entity
@Table(name = "food_portions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class FoodPortionRecord extends AbstractFoodPortion{

    @Id
    @SequenceGenerator(
            name = "food_portion_seq",
            sequenceName = "food_portion_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "food_portion_seq")
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealRecord meal;

    private FoodPortionRecord(MealRecord meal, Food food, Grams grams) {
        this.meal = Objects.requireNonNull(meal, "Meal cannot be null");
        this.food = Objects.requireNonNull(food, "Food cannot be null");
        this.grams = Objects.requireNonNull(grams, "Grams cannot be null");
    }

    public static FoodPortionRecord of(MealRecord mealPlan, Food food, Grams grams){
        return new FoodPortionRecord(mealPlan, food, grams);
    }

}

