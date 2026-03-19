package com.lirium.nutrition.model.enums;

public enum MealType {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    SNACK("Snack"),
    DINNER("Dinner"),
    DESSERT("Dessert");


    private final String label;
    MealType(String label) { this.label = label; }
    public String getLabel() { return label; }

}