package com.lirium.nutrition.model.valueobject;

import java.util.Objects;
import java.util.Optional;

/**
 * How close a day's intake was to what the plan prescribed, from 0 to 100 per nutrient. 100 is an
 * exact match; the score drops symmetrically for under- and over-eating. The overall score weights
 * calories at 40% and each macro at 20%: illustrative weights, not clinical guidance.
 */
public record NutritionScore(
    double calories, double protein, double carbs, double fat, double overall) {

  private static final double CALORIES_WEIGHT = 0.4;
  private static final double MACRO_WEIGHT = 0.2;

  /** Empty when nothing was planned for the day: there is nothing to compare against. */
  public static Optional<NutritionScore> of(NutrientBudget planned, NutrientBudget consumed) {
    Objects.requireNonNull(planned, "Planned nutrients cannot be null");
    Objects.requireNonNull(consumed, "Consumed nutrients cannot be null");

    if (planned.calories().amount() <= 0) {
      return Optional.empty();
    }

    double calories = closeness(planned.calories().amount(), consumed.calories().amount());
    double protein = closeness(planned.protein().grams(), consumed.protein().grams());
    double carbs = closeness(planned.carbs().amount(), consumed.carbs().amount());
    double fat = closeness(planned.fat().amount(), consumed.fat().amount());
    double overall = CALORIES_WEIGHT * calories + MACRO_WEIGHT * (protein + carbs + fat);

    return Optional.of(
        new NutritionScore(
            round(calories), round(protein), round(carbs), round(fat), round(overall)));
  }

  static double closeness(int planned, int consumed) {
    if (planned <= 0) {
      return consumed == 0 ? 100.0 : 0.0;
    }
    double ratio = consumed * 100.0 / planned;
    return Math.max(0.0, 100.0 - Math.abs(100.0 - ratio));
  }

  private static double round(double value) {
    return Math.round(value * 10.0) / 10.0;
  }
}
