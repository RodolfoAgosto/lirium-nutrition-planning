package com.lirium.nutrition.model.valueobject;

public record NutrientBudget(Calories calories, Carbs carbs, Fat fat, Protein protein) {

  public NutrientBudget subtract(NutrientBudget consumed) {
    return new NutrientBudget(
        this.calories.subtract(consumed.calories()),
        this.carbs.subtract(consumed.carbs()),
        this.fat.subtract(consumed.fat()),
        this.protein.subtract(consumed.protein()));
  }

  public NutrientBudget subtractClamped(NutrientBudget consumed) {
    return new NutrientBudget(
        new Calories(Math.max(0, this.calories.amount() - consumed.calories().amount())),
        new Carbs(Math.max(0, this.carbs.amount() - consumed.carbs().amount())),
        new Fat(Math.max(0, this.fat.amount() - consumed.fat().amount())),
        new Protein(Math.max(0, this.protein.grams() - consumed.protein().grams())));
  }
}
