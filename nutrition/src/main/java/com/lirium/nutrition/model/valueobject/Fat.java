package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Fat(int amount) {

    private static final int MIN_GRAMS = 0;
    private static final int MAX_GRAMS = 2000;

    public Fat {
        if (amount < MIN_GRAMS || amount > MAX_GRAMS) {
            throw new IllegalArgumentException(
                    String.format("Fat must be between %d and %d grams",
                            MIN_GRAMS, MAX_GRAMS)
            );
        }
    }

    public Fat add(Fat other) {
        Objects.requireNonNull(other);
        int result = Math.addExact(this.amount, other.amount);
        return new Fat(result);
    }

    public double toCalories() {
        return amount * 9.0;
    }

    public String toDisplayString() {
        return amount + " g";
    }

}
