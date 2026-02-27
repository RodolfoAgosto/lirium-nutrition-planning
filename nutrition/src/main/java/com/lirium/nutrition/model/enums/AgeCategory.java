package com.lirium.nutrition.model.enums;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents age categories for patients.
 * The label field is for UI display, while enum names are used internally.
 */

public enum AgeCategory {

    CHILD("Child"),
    ADOLESCENT("Adolescent"),
    ADULT("Adult"),
    OLDER_ADULT("Older Adult");

    private final String label;

    AgeCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AgeCategory from(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 12) return CHILD;
        if (age < 18) return ADOLESCENT;
        if (age < 60) return ADULT;
        return OLDER_ADULT;
    }
}