package com.lirium.nutrition.model.entity;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(nullable = false)
    protected Double quantity;

    @Enumerated(EnumType.STRING)
    protected MeasureUnit unit;

    protected AbstractFoodPortion() {}

    protected AbstractFoodPortion(Food food, Double quantity, MeasureUnit unit) {
        this.food = Objects.requireNonNull(food, "Food cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        if (quantity.compareTo(0.0) <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
    }

    public Double grams() {
        return food.toGrams(quantity, unit);
    }

    public Calories calories() {
        return  new Calories((int)(food.getCaloriesPer100g() * food.toGrams(this.getQuantity(), this.getUnit()) / 100));
    }

    public Carbs carbs() {
        return  new Carbs((int)(food.getCarbsPer100g() * food.toGrams(this.getQuantity(), this.getUnit()) / 100));
    }

    public Fat fat() {
        return  new Fat((int)(food.getFatPer100g() * food.toGrams(this.getQuantity(), this.getUnit()) / 100));
    }
    public Protein protein() {
        return  new Protein((int)(food.getProteinPer100g() * food.toGrams(this.getQuantity(), this.getUnit()) / 100));
    }

}