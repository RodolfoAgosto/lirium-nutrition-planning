package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Calories(int value) {

    private static final int MIN_CALORIES = 0;
    private static final int MAX_CALORIES = 200000;

    public Calories {
        if (value < MIN_CALORIES || value > MAX_CALORIES) {
            throw new IllegalArgumentException(
                    String.format("Calories must be between %d and %d",
                            MIN_CALORIES, MAX_CALORIES)
            );
        }
    }

    public Calories add(Calories other) {
        Objects.requireNonNull(other, "Calories to add cannot be null");
        int result = Math.addExact(this.value, other.value);
        return new Calories(result);
    }

    public boolean isZero() {
        return value == 0;
    }

    public String toDisplayString() {
        return value + " kcal";
    }

}