package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import com.lirium.nutrition.service.PlanMealAssembler;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PlanMealAssemblerImpl implements PlanMealAssembler {

  private final PlanFoodPortionAssembler planFoodPortionAssembler;

  public PlanMealAssemblerImpl(PlanFoodPortionAssembler planFoodPortionAssembler) {
    this.planFoodPortionAssembler = planFoodPortionAssembler;
  }

  @Override
  public void assemble(
      DailyPlan dailyPlan,
      PatientProfile patientProfile,
      Calories calories,
      MacroDistribution macros,
      Set<Long> usedFoodIdsInDay) {
    assemble(dailyPlan, patientProfile, calories, macros, Collections.emptySet(), usedFoodIdsInDay);
  }

  @Override
  public void assemble(
      DailyPlan dailyPlan,
      PatientProfile patientProfile,
      Calories calories,
      MacroDistribution macros,
      Set<FoodTag> additionalExcludedTags,
      Set<Long> usedFoodIdsInDay) {

    Set<FoodTag> excludedTags =
        new HashSet<>(resolveExcludedTags(patientProfile.getRestrictions()));
    excludedTags.addAll(additionalExcludedTags);

    NutrientBudget nutrientBudgetTarget =
        new NutrientBudget(
            calories,
            new Carbs(macros.carbGrams()),
            new Fat(macros.fatGrams()),
            new Protein(macros.proteinGrams()));

    NutrientBudget nutrientBudgetRemaining = nutrientBudgetTarget;

    for (MealType meal : MealType.values()) {
      PlanMeal planMeal = PlanMeal.of(meal, dailyPlan);

      NutrientBudget mealBudget =
          calculateMealBudget(meal, nutrientBudgetTarget, nutrientBudgetRemaining);

      NutrientBudget consumed =
          planFoodPortionAssembler.assemble(planMeal, mealBudget, excludedTags, usedFoodIdsInDay);

      nutrientBudgetRemaining = nutrientBudgetRemaining.subtract(consumed);

      dailyPlan.addMeal(planMeal);
    }
  }

  private Set<FoodTag> resolveExcludedTags(Set<Restriction> restrictions) {
    return restrictions.stream()
        .flatMap(r -> r.getExcludedTags().stream())
        .collect(Collectors.toSet());
  }

  private NutrientBudget calculateMealBudget(
      MealType mealType, NutrientBudget target, NutrientBudget remaining) {

    // Obtiene las comidas restantes desde la comida actual en adelante
    List<MealType> remainingMeals =
        Arrays.stream(MealType.values()).dropWhile(m -> m != mealType).toList();

    double sumCalorieRatio = remainingMeals.stream().mapToDouble(MealType::getCalorieRatio).sum();
    double sumCarbRatio = remainingMeals.stream().mapToDouble(MealType::getCarbRatio).sum();
    double sumFatRatio = remainingMeals.stream().mapToDouble(MealType::getFatRatio).sum();
    double sumProteinRatio = remainingMeals.stream().mapToDouble(MealType::getProteinRatio).sum();

    int mealCalories =
        calculateComponentBudget(
            remaining.calories().amount(), mealType.getCalorieRatio(), sumCalorieRatio);

    int mealCarbs =
        calculateComponentBudget(remaining.carbs().amount(), mealType.getCarbRatio(), sumCarbRatio);

    int mealFat =
        calculateComponentBudget(remaining.fat().amount(), mealType.getFatRatio(), sumFatRatio);

    int mealProtein =
        calculateComponentBudget(
            remaining.protein().grams(), mealType.getProteinRatio(), sumProteinRatio);

    return new NutrientBudget(
        new Calories(mealCalories),
        new Carbs(mealCarbs),
        new Fat(mealFat),
        new Protein(mealProtein));
  }

  private int calculateComponentBudget(
      int remainingAmount, double mealRatio, double totalRemainingRatio) {

    if (totalRemainingRatio <= 0 || remainingAmount <= 0) {
      return Math.max(0, remainingAmount);
    }

    double proportionalTarget = (mealRatio * remainingAmount) / totalRemainingRatio;
    return (int) Math.round(proportionalTarget);
  }
}
