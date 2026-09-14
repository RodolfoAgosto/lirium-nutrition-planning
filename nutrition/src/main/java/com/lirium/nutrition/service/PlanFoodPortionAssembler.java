package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.valueobject.*;
import java.util.Set;

public interface PlanFoodPortionAssembler {

  NutrientBudget assemble(
      PlanMeal planMeal, NutrientBudget nutrientBudget, Set<Long> usedFoodIdsInDay);

  NutrientBudget assemble(
      PlanMeal planMeal,
      NutrientBudget nutrientBudget,
      Set<FoodTag> additionalExcludedTags,
      Set<Long> usedFoodIdsInDay);
}
