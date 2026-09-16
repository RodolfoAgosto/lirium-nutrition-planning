package com.lirium.nutrition.model.valueobject;

public record MacroDeviation(double calories, double carbs, double fat, double protein) {

  public static final MacroDeviation ZERO = new MacroDeviation(0, 0, 0, 0);
}
