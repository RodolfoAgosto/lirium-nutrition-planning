package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Fat(int value) {

    private static final int MIN_GRAMS = 0;
    private static final int MAX_GRAMS = 2000;

    public Fat {
        if (value < MIN_GRAMS || value > MAX_GRAMS) {
            throw new IllegalArgumentException(
                    String.format("Fat must be between %d and %d grams",
                            MIN_GRAMS, MAX_GRAMS)
            );
        }
    }

    public Fat add(Fat other) {
        Objects.requireNonNull(other);
        int result = Math.addExact(this.value, other.value);
        return new Fat(result);
    }

    public double toCalories() {
        return value * 9.0;
    }

    public String toDisplayString() {
        return value + " g";
    }

}
