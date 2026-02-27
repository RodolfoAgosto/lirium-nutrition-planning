package com.lirium.nutrition.model.enums;

/**
 * Represents the activity level of a patient.
 * `label` is used for display in UI while enum name is used internally.
 */

public enum ActivityLevel {

    SEDENTARY("Sedentary"),
    MODERATE("Moderate"),
    ACTIVE("Active"),
    VERY_ACTIVE("Very Active");

    private final String label;

    ActivityLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}