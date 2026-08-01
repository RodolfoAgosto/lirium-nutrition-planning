package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Carbs;
import com.lirium.nutrition.model.valueobject.Fat;
import com.lirium.nutrition.model.valueobject.Protein;
import jakarta.persistence.*;
import lombok.Getter;

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

    @Column(nullable = false, name = "quantity")
    protected Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "measure_unit")
    protected MeasureUnit measureUnit;

    protected AbstractFoodPortion() {}

    protected AbstractFoodPortion(Food food, Double quantity, MeasureUnit unit) {
        this.food = Objects.requireNonNull(food, "Food cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.measureUnit = Objects.requireNonNull(unit, "Unit cannot be null");
        if (quantity.compareTo(0.0) <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
    }

    public Double grams() {
        return food.toGrams(quantity, measureUnit);
    }

    public Calories calories() {
        return  new Calories((int)(food.getCaloriesPer100g() * food.toGrams(this.getQuantity(), this.getMeasureUnit()) / 100));
    }

    public Carbs carbs() {
        return  new Carbs((int)(food.getCarbsPer100g() * food.toGrams(this.getQuantity(), this.getMeasureUnit()) / 100));
    }

    public Fat fat() {
        return  new Fat((int)(food.getFatPer100g() * food.toGrams(this.getQuantity(), this.getMeasureUnit()) / 100));
    }

    public Protein protein() {
        return  new Protein((int)(food.getProteinPer100g() * food.toGrams(this.getQuantity(), this.getMeasureUnit()) / 100));
    }

}