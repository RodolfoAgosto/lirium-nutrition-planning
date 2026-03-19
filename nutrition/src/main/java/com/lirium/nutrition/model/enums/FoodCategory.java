package com.lirium.nutrition.model.enums;

public enum FoodCategory {

    PROTEIN("Protein sources"),
    CARB("Carbohydrates"),
    DAIRY("Dairy products"),
    VEGETABLE("Vegetables"),
    FRUIT("Fruits"),
    SWEET("Sweets and desserts"),
    FAT("Fats and oils"),
    BEVERAGE("Beverages");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
