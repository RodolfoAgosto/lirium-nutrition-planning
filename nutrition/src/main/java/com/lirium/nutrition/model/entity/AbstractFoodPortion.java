package com.lirium.nutrition.model.entity;
import com.lirium.nutrition.model.valueobject.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;

/**
 * Represents a quantified portion of a specific Food.
 * This abstract base class is reused by entities that need to model food quantities
 * (e.g. meal items, recipe ingredients, historical records).
 * A portion references a Food entity and a validated amount expressed in grams.
 * Subclasses define the lifecycle and ownership of the portion.
 */

@Getter
@MappedSuperclass
public abstract class AbstractFoodPortion {

    @ManyToOne(fetch = FetchType.LAZY, optional = false )
    @JoinColumn(name = "food_id", nullable = false)
    protected Food food;

    @Embedded
    @AttributeOverride(
            name = "grams",
            column = @Column(name = "grams", nullable = false)
    )
    protected Grams grams;

    protected AbstractFoodPortion() {}

    public AbstractFoodPortion(Food food, Grams grams) {
        this.food = Objects.requireNonNull(food);
        this.grams = Objects.requireNonNull(grams);
    }

    public Calories calories() {
        return  new Calories(food.getCaloriesPer100g() * grams.value() / 100);
    }

    public Carbs carbs() {
        return  new Carbs(food.getCarbsPer100g() * grams.value() / 100);
    }

    public Fat fat() {
        return  new Fat(food.getFatPer100g() * grams.value() / 100);
    }

    public Protein protein() {
        return  new Protein(food.getProteinPer100g() * grams.value() / 100);
    }

}