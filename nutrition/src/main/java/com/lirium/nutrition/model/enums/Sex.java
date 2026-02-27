package com.lirium.nutrition.model.enums;

/**
 * Represents biological sex for a patient.
 * The enum value is used internally, while `label` is used for UI display.
 */
public enum Sex {

    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String label;

    Sex(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}