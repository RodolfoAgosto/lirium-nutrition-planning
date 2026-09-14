package com.lirium.nutrition.model.valueobject;

public record NutrientBudget(Calories calories, Carbs carbs, Fat fat, Protein protein) {

  public NutrientBudget subtract(NutrientBudget consumed) {
    return new NutrientBudget(
        this.calories.subtract(consumed.calories()),
        this.carbs.subtract(consumed.carbs()),
        this.fat.subtract(consumed.fat()),
        this.protein.subtract(consumed.protein()));
  }
}
