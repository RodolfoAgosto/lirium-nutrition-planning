package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.valueobject.*;
import java.util.Map;
import java.util.Set;

public interface PlanFoodPortionAssembler {

  MealAssemblyResult assemble(
      PlanMeal planMeal,
      NutrientBudget target,
      Set<Long> usedFoodIdsInDay,
      Map<Long, Integer> foodFrequencyInWeek,
      Map<Long, Double> foodGramsInDay);

  MealAssemblyResult assemble(
      PlanMeal planMeal,
      NutrientBudget target,
      Set<FoodTag> additionalExcludedTags,
      Set<Long> usedFoodIdsInDay,
      Map<Long, Integer> foodFrequencyInWeek,
      Map<Long, Double> foodGramsInDay);
}
