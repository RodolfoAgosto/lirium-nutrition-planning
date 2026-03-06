package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Carbs(int value) {

    private static final int MIN_CARBS = 0;
    private static final int MAX_CARBS = 1000;

    public Carbs {
        if (value < MIN_CARBS || value > MAX_CARBS) {
            throw new IllegalArgumentException(
                    String.format("Carbs must be between %d and %d grams",
                            MIN_CARBS, MAX_CARBS)
            );
        }
    }

    public Carbs add(Carbs other) {
        Objects.requireNonNull(other, "Carbs cannot be null");
        int result = Math.addExact(this.value, other.value);
        return new Carbs(result);
    }

    public int toCalories() {
        return value * 4;
    }

    public boolean isZero() {
        return value == 0;
    }

    public String toDisplayString() {
        return value + " g";
    }

}
