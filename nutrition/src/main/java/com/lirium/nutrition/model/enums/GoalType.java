package com.lirium.nutrition.model.enums;

public enum GoalType {
    WEIGHT_LOSS("Weight loss"),
    MUSCLE_GAIN("Muscle gain"),
    WEIGHT_MAINTENANCE("Weight maintenance"),
    METABOLIC_HEALTH("Metabolic health"),
    PREGNANCY_HEALTH("Pregnancy health"),
    LACTATION_HEALTH("Lactation health");

    private final String label;
    GoalType(String label) { this.label = label; }
    public String getLabel() { return label; }
}