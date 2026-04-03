package com.lirium.nutrition.model.enums;

public enum MealType {

    BREAKFAST("Breakfast", 0.25, 0.25, 0.30, 0.25),
    MID_MORNING("Mid Morning", 0.10, 0.10, 0.10, 0.10),
    LUNCH("Lunch", 0.30, 0.30, 0.30, 0.30),
    SNACK("Snack", 0.15, 0.15, 0.15, 0.15),
    DINNER("Dinner", 0.20, 0.20, 0.15, 0.20);

    private final String label;
    private final double calorieRatio;
    private final double proteinRatio;
    private final double carbRatio;
    private final double fatRatio;

    MealType(String label, double calorieRatio, double proteinRatio, double carbRatio, double fatRatio) {
        this.label = label;
        this.calorieRatio = calorieRatio;
        this.proteinRatio = proteinRatio;
        this.carbRatio = carbRatio;
        this.fatRatio = fatRatio;
    }

    public String getLabel() { return label; }
    public double getCalorieRatio() { return calorieRatio; }
    public double getProteinRatio() { return proteinRatio; }
    public double getCarbRatio()    { return carbRatio; }
    public double getFatRatio()     { return fatRatio; }
}