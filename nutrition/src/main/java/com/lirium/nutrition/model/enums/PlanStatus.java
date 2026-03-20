package com.lirium.nutrition.model.enums;

public enum PlanStatus {

    DRAFT("Draft — being prepared"),
    ACTIVE("Active — assigned to patient"),
    INACTIVE("Inactive — replaced by newer plan");

    private final String label;

    PlanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}