package com.lirium.nutrition.model.enums;

public enum PhysiologicalCondition {
    PREGNANCY("Pregnancy"),
    LACTATION("Lactation");

    private final String label;
    PhysiologicalCondition(String label) { this.label = label; }
    public String getLabel() { return label; }
}