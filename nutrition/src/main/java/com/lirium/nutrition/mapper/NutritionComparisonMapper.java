package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.response.NutrientsDTO;
import com.lirium.nutrition.dto.response.NutritionScoreDTO;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.valueobject.NutrientBudget;
import com.lirium.nutrition.model.valueobject.NutritionScore;

public class NutritionComparisonMapper {

  private NutritionComparisonMapper() {}

  public static NutrientsDTO toNutrients(NutrientBudget budget) {
    return new NutrientsDTO(
        budget.calories().amount(),
        budget.protein().grams(),
        budget.carbs().amount(),
        budget.fat().amount());
  }

  public static NutrientsDTO targetsOf(NutritionPlan plan) {
    return new NutrientsDTO(
        plan.getDailyCalories(), plan.getProteinGrams(), plan.getCarbGrams(), plan.getFatGrams());
  }

  public static NutritionScoreDTO toScore(NutritionScore score) {
    return new NutritionScoreDTO(
        score.calories(), score.protein(), score.carbs(), score.fat(), score.overall());
  }
}
