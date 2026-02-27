package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public record Weight(int gr) {

    private static final int MIN_WEIGHT_GR = 300;
    private static final int MAX_WEIGHT_GR = 300_000;

    public static Weight of(int value) {
        return new Weight(value);
    }

    public Weight {
        if (gr < MIN_WEIGHT_GR || gr > MAX_WEIGHT_GR) {
            throw new IllegalArgumentException(
                    String.format(
                            "Weight must be between %d and %d grams",
                            MIN_WEIGHT_GR,
                            MAX_WEIGHT_GR
                    )
            );
        }
    }

    public double toKg() {
        return gr / 1000.0;
    }

    public String toDisplayString() {
        return String.format("%.2f kg", toKg());
    }

}
