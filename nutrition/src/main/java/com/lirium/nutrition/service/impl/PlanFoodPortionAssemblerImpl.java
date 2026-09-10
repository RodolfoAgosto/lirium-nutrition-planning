package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Carbs;
import com.lirium.nutrition.model.valueobject.Fat;
import com.lirium.nutrition.model.valueobject.Protein;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlanFoodPortionAssemblerImpl implements PlanFoodPortionAssembler {

  private final Map<MealType, List<SlotDistribution>> distributions;
  private final FoodRepository foodRepository;

  private static final double DEFAULT_MIN_SERVING_GRAMS = 15.0;
  private static final double DEFAULT_MAX_SERVING_GRAMS = 250.0;

  public PlanFoodPortionAssemblerImpl(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.distributions = buildDistributions();
  }

  private Map<MealType, List<SlotDistribution>> buildDistributions() {
    return Map.of(
        MealType.BREAKFAST,
            List.of(
                new SlotDistribution(FoodCategory.DAIRY),
                new SlotDistribution(FoodCategory.CARB),
                new SlotDistribution(FoodCategory.BEVERAGE)),
        // ORDEN CLAVE: CARB y VEGETABLE antes que PROTEIN para absorber la proteína indirecta
        // primero
        MealType.LUNCH,
            List.of(
                new SlotDistribution(FoodCategory.CARB),
                new SlotDistribution(FoodCategory.VEGETABLE),
                new SlotDistribution(FoodCategory.PROTEIN),
                new SlotDistribution(FoodCategory.SWEET),
                new SlotDistribution(FoodCategory.FAT),
                new SlotDistribution(FoodCategory.BEVERAGE)),
        MealType.MID_MORNING,
            List.of(
                new SlotDistribution(FoodCategory.DAIRY), new SlotDistribution(FoodCategory.FRUIT)),
        MealType.SNACK,
            List.of(
                new SlotDistribution(FoodCategory.DAIRY),
                new SlotDistribution(FoodCategory.CARB),
                new SlotDistribution(FoodCategory.BEVERAGE)),
        // ORDEN CLAVE: CARB/VEGETABLE/FRUIT antes que PROTEIN
        MealType.DINNER,
            List.of(
                new SlotDistribution(FoodCategory.CARB),
                new SlotDistribution(FoodCategory.VEGETABLE),
                new SlotDistribution(FoodCategory.FRUIT),
                new SlotDistribution(FoodCategory.PROTEIN),
                new SlotDistribution(FoodCategory.BEVERAGE),
                new SlotDistribution(FoodCategory.FAT)));
  }

  record SlotDistribution(FoodCategory category) {}

  @Override
  public void assemble(
      PlanMeal planMeal,
      PatientProfile patient,
      Calories calories,
      Fat fat,
      Carbs carbs,
      Protein protein) {
    assemble(planMeal, patient, Collections.emptySet(), calories, fat, carbs, protein);
  }

  @Override
  public void assemble(
      PlanMeal planMeal,
      PatientProfile patient,
      Set<FoodTag> additionalExcludedTags,
      Calories calories,
      Fat fat,
      Carbs carbs,
      Protein protein) {

    // Determine which foods are prohibited.
    Set<FoodTag> excludedTags = new HashSet<>(resolveExcludedTags(patient.getRestrictions()));
    excludedTags.addAll(additionalExcludedTags);

    // Look for suitable foods for that meal.
    List<Food> availableFoods =
        new ArrayList<>(foodRepository.findSuitableFoods(planMeal.getType(), excludedTags));
    Collections.shuffle(availableFoods);

    // It obtains the categories corresponding to the meal and, if it is mid-morning, selects a
    // single category at random.
    List<SlotDistribution> slots =
        new ArrayList<>(distributions.getOrDefault(planMeal.getType(), Collections.emptyList()));
    if (planMeal.getType() == MealType.MID_MORNING && !slots.isEmpty()) {
      Collections.shuffle(slots);
      slots = List.of(slots.get(0));
    }

    // Remaining calories and macronutrients to cover as foods are added to the meal.
    double remCal = calories.amount();
    double remCarb = carbs.amount();
    double remFat = fat.amount();
    double remProt = protein.grams();

    for (SlotDistribution slot : slots) {
      if (remCal <= 0 && remProt <= 0 && remCarb <= 0 && remFat <= 0) {
        break;
      }

      // Selects the first available food matching the current slot category.
      Optional<Food> foodOpt =
          availableFoods.stream().filter(f -> f.getCategory() == slot.category()).findFirst();
      if (foodOpt.isEmpty()) {
        log.warn("No food found for category={} in meal={}", slot.category(), planMeal.getType());
        continue;
      }
      Food food = foodOpt.get();
      availableFoods.remove(food);

      // 1. Calculates the theoretical amount needed to cover the remaining macro target.
      double rawGrams = calculateGrams(food, remCal, remCarb, remFat, remProt);
      if (rawGrams <= 0) {
        continue;
      }

      // 2. Restricts the calculated amount to the food's configured serving limits.
      double boundedGrams = clampGramsToFoodLimits(food, rawGrams);

      // 3. Converts the amount to the food's configured unit (grams, milliliters, or units).
      double finalQuantity = convertToFinalUnit(food, boundedGrams);
      MeasureUnit finalUnit = food.getDefaultUnit();

      // 4. Converts the final quantity back to grams to calculate the food's actual macro
      // contribution.
      double actualGrams =
          (finalUnit == MeasureUnit.UNIT)
              ? finalQuantity * getUnitWeightInGrams(food)
              : finalQuantity;

      // 5. Creates the portion and adds it to the meal.
      PlanFoodPortion portion = PlanFoodPortion.of(planMeal, food, finalQuantity, finalUnit);
      planMeal.addFoodPortion(portion);

      // 5. Descuento de macros acumulados
      remCal -= (food.getCaloriesPer100g() * actualGrams) / 100.0;
      remCarb -= (food.getCarbsPer100g() * actualGrams) / 100.0;
      remFat -= (food.getFatPer100g() * actualGrams) / 100.0;
      remProt -= (food.getProteinPer100g() * actualGrams) / 100.0;

      log.info(
          "Meal={} Food={} grams={} | remaining: cal={} carb={} fat={} prot={}",
          planMeal.getType(),
          food.getName(),
          actualGrams,
          remCal,
          remCarb,
          remFat,
          remProt);
    }
  }

  private double calculateGrams(
      Food food, double targetCal, double targetCarb, double targetFat, double targetProt) {
    FoodCategory category = food.getCategory();

    // Skip categories whose primary macro target has already been reached.

    // If I no longer need protein and I am trying to add a food whose primary function
    // is to provide protein, I do not add that food.
    if (targetProt <= 0 && (category == FoodCategory.PROTEIN || category == FoodCategory.DAIRY)) {
      return 0.0;
    }
    if (targetCarb <= 0 && (category == FoodCategory.CARB || category == FoodCategory.SWEET)) {
      return 0.0;
    }
    if (targetFat <= 0 && category == FoodCategory.FAT) {
      return 0.0;
    }

    // Avoid adding a protein food when less than 8 g of protein remain,
    // since the minimum serving could cause an unnecessary protein excess.
    if (category == FoodCategory.PROTEIN && targetProt < 8.0) {
      return 0.0;
    }

    // Calculate the amount based on the macro that defines the food category.
    return switch (category) {
      case PROTEIN, DAIRY ->
          food.getProteinPer100g() > 0 ? (targetProt * 100.0) / food.getProteinPer100g() : 100.0;

      case CARB, SWEET ->
          food.getCarbsPer100g() > 0 ? (targetCarb * 100.0) / food.getCarbsPer100g() : 100.0;

      case FAT ->
          food.getFatPer100g() > 0
              ? (targetFat * 100.0) / food.getFatPer100g()
              : 15.0; // Fallback base (ej: 1 cucharada de aceite / 15g frutos secos)

      case VEGETABLE, FRUIT ->
          food.getDefaultUnit() == MeasureUnit.UNIT ? getUnitWeightInGrams(food) : 150.0;

      case BEVERAGE -> 200.0;
    };
  }

  private double clampGramsToFoodLimits(Food food, double calculatedGrams) {
    double minGrams =
        (food.getMinServingGrams() != null) ? food.getMinServingGrams() : DEFAULT_MIN_SERVING_GRAMS;

    double maxGrams =
        (food.getMaxServingGrams() != null) ? food.getMaxServingGrams() : DEFAULT_MAX_SERVING_GRAMS;

    return Math.min(Math.max(calculatedGrams, minGrams), maxGrams);
  }

  private double convertToFinalUnit(Food food, double boundedGrams) {
    MeasureUnit unit = food.getDefaultUnit();

    return switch (unit) {
      case GRAM -> Math.round(boundedGrams);
      case MILLILITER ->
          Math.round(boundedGrams / (food.getDensity() != null ? food.getDensity() : 1.0));
      case UNIT -> {
        double unitWeight = getUnitWeightInGrams(food);
        double exactUnits = boundedGrams / unitWeight;
        yield Math.max(1.0, Math.round(exactUnits)); // Fuerza enteros (1, 2, 3 unidades)
      }
    };
  }

  private double getUnitWeightInGrams(Food food) {
    if (food.getUnitWeight() != null && food.getUnitWeight() > 0) {
      return food.getUnitWeight();
    }
    return 100.0; // If there is no valid weight per unit, 100 g per unit is assumed.
  }

  // Combines all excluded tags from the patient's restrictions into a single set.
  private Set<FoodTag> resolveExcludedTags(Set<Restriction> restrictions) {
    return restrictions.stream()
        .flatMap(r -> r.getExcludedTags().stream())
        .collect(Collectors.toSet());
  }
}
