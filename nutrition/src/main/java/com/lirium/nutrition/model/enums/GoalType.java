package com.lirium.nutrition.model.enums;

public enum GoalType {

    WEIGHT_LOSS("Weight loss") {
        @Override
        public double adjust(double calories) {
            return calories * 0.8;
        }
    },
    MUSCLE_GAIN("Muscle gain") {
        @Override
        public double adjust(double calories) {
            return calories * 1.15;
        }
    },
    WEIGHT_MAINTENANCE("Weight maintenance") {
        @Override
        public double adjust(double calories) {
            return calories;
        }
    },
    METABOLIC_HEALTH("Metabolic health") {
        @Override
        public double adjust(double calories) {
            return calories;
        }
    },
    PREGNANCY_HEALTH("Pregnancy health") {
        @Override
        public double adjust(double calories) {
            return calories + 300;
        }
    },
    LACTATION_HEALTH("Lactation health") {
        @Override
        public double adjust(double calories) {
            return calories + 500;
        }
    };

    private final String label;

    GoalType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract double adjust(double calories);
}
