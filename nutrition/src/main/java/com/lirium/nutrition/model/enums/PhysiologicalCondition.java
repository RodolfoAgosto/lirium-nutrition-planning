package com.lirium.nutrition.model.enums;

public enum PhysiologicalCondition {

    PREGNANCY("Pregnancy"){
        @Override
        public double adjust(double calories) {
            return calories + 300;
        }
    },
    LACTATION("Lactation"){
        @Override
        public double adjust(double calories) {
            return calories + 500;
        }
    },
    MENOPAUSE("Menopause"){
        @Override
        public double adjust(double calories) {
            return calories;
        }
    };

    private final String label;

    PhysiologicalCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract double adjust(double calories);
}