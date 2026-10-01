package com.lirium.nutrition.model.valueobject;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

public record NutrientBudget(Calories calories, Carbs carbs, Fat fat, Protein protein) {

  public static final NutrientBudget ZERO =
      new NutrientBudget(new Calories(0), new Carbs(0), new Fat(0), new Protein(0));

  public NutrientBudget add(NutrientBudget other) {
    java.util.Objects.requireNonNull(other, "NutrientBudget to add cannot be null");
    return new NutrientBudget(
        this.calories.add(other.calories()),
        this.carbs.add(other.carbs()),
        this.fat.add(other.fat()),
        this.protein.add(other.protein()));
  }

  /** Average of the given budgets, each nutrient rounded to the nearest unit. */
  public static Optional<NutrientBudget> average(List<NutrientBudget> budgets) {
    if (budgets.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new NutrientBudget(
            new Calories(roundedAverage(budgets, b -> b.calories().amount())),
            new Carbs(roundedAverage(budgets, b -> b.carbs().amount())),
            new Fat(roundedAverage(budgets, b -> b.fat().amount())),
            new Protein(roundedAverage(budgets, b -> b.protein().grams()))));
  }

  private static int roundedAverage(
      List<NutrientBudget> budgets, ToIntFunction<NutrientBudget> nutrient) {
    return (int) Math.round(budgets.stream().mapToInt(nutrient).average().orElse(0));
  }

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
