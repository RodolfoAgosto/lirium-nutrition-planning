package com.lirium.nutrition.model.enums;

/**
 * Tags representing characteristics or contents of a food item.
 * The enum value is used internally, while `label` is used for UI display.
 */

public enum FoodTag {

    GLUTEN("Gluten (wheat, barley, rye, oats)"),
    LACTOSE("Lactose (milk, cheese, yogurt)"),
    MEAT("Meat (beef, pork, lamb)"),
    FISH("Fish / Seafood"),
    EGG("Eggs"),
    HONEY("Honey"),
    GELATIN("Gelatin (animal-derived)"),
    NUTS("Nuts (peanuts, walnuts, etc.)"),
    SOY("Soy products"),
    ALCOHOL("Alcohol");

    private final String label;

    FoodTag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}