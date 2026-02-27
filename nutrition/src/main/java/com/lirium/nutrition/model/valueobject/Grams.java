package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public record Grams(int value) {

    private static final int MIN_GRAMS = 0;

    public Grams {
        if (value < MIN_GRAMS) {
            throw new IllegalArgumentException(
                    String.format(
                            "Grams must be greater than or equal to %d", MIN_GRAMS
                    )
            );
        }
    }

}