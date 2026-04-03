package com.lirium.nutrition.model.enums;

/**
 * Represents the activity level of a patient.
 * `label` is used for display in UI while enum name is used internally.
 */
public enum ActivityLevel {

    SEDENTARY(1.2, 0.8, 0.8, 0.30, 0.30),
    MODERATE(1.55, 1.2, 1.1, 0.30, 0.32),
    ACTIVE(1.725, 1.6, 1.4, 0.25, 0.30),
    VERY_ACTIVE(1.9, 2.0, 1.8, 0.20, 0.28);

    private final double factor;

    private final double maleProtein;
    private final double femaleProtein;

    private final double maleFat;
    private final double femaleFat;

    ActivityLevel(double factor,
                  double maleProtein, double femaleProtein,
                  double maleFat, double femaleFat) {
        this.factor = factor;
        this.maleProtein = maleProtein;
        this.femaleProtein = femaleProtein;
        this.maleFat = maleFat;
        this.femaleFat = femaleFat;
    }

    public double proteinFactor(Sex sex) {
        return sex == Sex.MALE ? maleProtein : femaleProtein;
    }

    public double fatPercentage(Sex sex) {
        return sex == Sex.MALE ? maleFat : femaleFat;
    }

    public double getFactor() {
        return factor;
    }
}