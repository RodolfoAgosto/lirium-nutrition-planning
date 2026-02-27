package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public record Height(int cm) {

    private static final int MIN_HEIGHT = 30;
    private static final int MAX_HEIGHT = 250;

    public Height {
        if (cm < MIN_HEIGHT || cm > MAX_HEIGHT) {
            throw new IllegalArgumentException(
                    String.format(
                            "Height must be between %d and %d cm",
                            MIN_HEIGHT,
                            MAX_HEIGHT
                    )
            );
        }
    }

    public String toDisplayString() {
        return cm + " cm";
    }

}
