package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlanFoodPortionAssemblerImpl implements PlanFoodPortionAssembler {

  private final Map<MealType, List<SlotDistribution>> distributions;
  private final FoodRepository foodRepository;

  private static final double DEFAULT_MIN_SERVING_GRAMS = 10.0;
  private static final double DEFAULT_MAX_SERVING_GRAMS = 300.0;
  private static final double ZERO_GRAMS = 0.0;
  private static final double DEFAULT_MACRO_CALCULATION_GRAMS = 100.0;
  private static final double DEFAULT_FAT_PORTION_GRAMS = 15.0;
  private static final double DEFAULT_FRUIT_VEGETABLE_GRAMS = 150.0;
  private static final double DEFAULT_BEVERAGE_GRAMS = 200.0;

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
        MealType.LUNCH,
        List.of(
            new SlotDistribution(FoodCategory.PROTEIN),
            new SlotDistribution(FoodCategory.CARB),
            new SlotDistribution(FoodCategory.VEGETABLE),
            new SlotDistribution(FoodCategory.FAT),
            new SlotDistribution(FoodCategory.SWEET),
            new SlotDistribution(FoodCategory.BEVERAGE)),
        MealType.MID_MORNING,
        List.of(new SlotDistribution(FoodCategory.DAIRY), new SlotDistribution(FoodCategory.FRUIT)),
        MealType.SNACK,
        List.of(
            new SlotDistribution(FoodCategory.DAIRY),
            new SlotDistribution(FoodCategory.CARB),
            new SlotDistribution(FoodCategory.BEVERAGE)),
        MealType.DINNER,
        List.of(
            new SlotDistribution(FoodCategory.PROTEIN),
            new SlotDistribution(FoodCategory.CARB),
            new SlotDistribution(FoodCategory.VEGETABLE),
            new SlotDistribution(FoodCategory.FRUIT),
            new SlotDistribution(FoodCategory.FAT),
            new SlotDistribution(FoodCategory.BEVERAGE)));
  }

  record SlotDistribution(FoodCategory category) {}

  @Override
  public NutrientBudget assemble(
      PlanMeal planMeal, NutrientBudget nutrientBudget, Set<Long> usedFoodIdsInDay) {
    return assemble(planMeal, nutrientBudget, Collections.emptySet(), usedFoodIdsInDay);
  }

  @Override
  public NutrientBudget assemble(
      PlanMeal planMeal,
      NutrientBudget nutrientBudget,
      Set<FoodTag> excludedTags,
      Set<Long> usedFoodIdsInDay) {

    double caloriesConsumed = 0.0;
    double carbsConsumed = 0.0;
    double fatsConsumed = 0.0;
    double proteinConsumed = 0.0;

    List<Food> availableFoods =
        new ArrayList<>(foodRepository.findSuitableFoods(planMeal.getType(), excludedTags));
    Collections.shuffle(availableFoods);

    List<SlotDistribution> slots =
        new ArrayList<>(distributions.getOrDefault(planMeal.getType(), Collections.emptyList()));

    double remCal = nutrientBudget.calories().amount();
    double remCarb = nutrientBudget.carbs().amount();
    double remFat = nutrientBudget.fat().amount();
    double remProt = nutrientBudget.protein().grams();

    int totalSlots = slots.size();
    int currentSlotIndex = 0;

    for (SlotDistribution slot : slots) {
      currentSlotIndex++;
      int remainingSlotsInMeal = (totalSlots - currentSlotIndex) + 1;

      if (remCal <= 10.0 && remProt <= 2.0) {
        break;
      }

      Optional<Food> foodOpt =
          availableFoods.stream()
              .filter(f -> f.getCategory() == slot.category())
              .filter(f -> !usedFoodIdsInDay.contains(f.getId()))
              .findFirst();

      if (foodOpt.isEmpty()) {
        foodOpt =
            availableFoods.stream().filter(f -> f.getCategory() == slot.category()).findFirst();
      }

      if (foodOpt.isEmpty()) {
        log.warn("No food found for category={} in meal={}", slot.category(), planMeal.getType());
        continue;
      }

      Food food = foodOpt.get();
      availableFoods.remove(food);

      // Presupuesto target para este slot en particular (evita que el primer carb consuma el 100%
      // del budget de la comida)
      double targetCalForSlot = remCal / remainingSlotsInMeal;
      double targetCarbForSlot = remCarb / remainingSlotsInMeal;
      double targetProtForSlot = remProt / remainingSlotsInMeal;
      double targetFatForSlot = remFat / remainingSlotsInMeal;

      // 1. Calcular gramos teóricos requeridos
      double rawGrams =
          calculateGrams(
              food,
              remCal,
              remCarb,
              remFat,
              remProt,
              targetCalForSlot,
              targetCarbForSlot,
              targetProtForSlot,
              targetFatForSlot);
      if (rawGrams <= 0) {
        continue;
      }

      // 2. Acotar según límites de porción del alimento y calorías restantes
      double boundedGrams = clampGramsToFoodLimits(food, rawGrams, remCal);
      if (boundedGrams <= 0) {
        continue;
      }

      // 3. Convertir a unidades finales (Gramos, Mililitros, Unidades fraccionables)
      double finalQuantity = convertToFinalUnit(food, boundedGrams);
      MeasureUnit finalUnit = food.getDefaultUnit();

      // 4. Calcular los gramos reales representados por la cantidad redondeada
      double actualGrams = calculateActualGrams(food, finalQuantity, finalUnit);

      // 5. Calcular macronutrientes aportados por la porción real
      double addedCal = (food.getCaloriesPer100g() * actualGrams) / 100.0;
      double addedCarb = (food.getCarbsPer100g() * actualGrams) / 100.0;
      double addedFat = (food.getFatPer100g() * actualGrams) / 100.0;
      double addedProt = (food.getProteinPer100g() * actualGrams) / 100.0;

      // GUARD CLAUSE FLEXIBLE: Permite pequeños excesos en el macro secundario si el principal es
      // necesario
      double maxAllowedCal = remCal + 50.0; // Margen de tolerancia para cerrar plato
      if (addedCal > maxAllowedCal && food.getCategory() != FoodCategory.VEGETABLE) {
        log.warn(
            "Skipping {} in {} because addedCal ({}) exceeds remaining cal limit ({})",
            food.getName(),
            planMeal.getType(),
            addedCal,
            maxAllowedCal);
        continue;
      }

      // 6. Crear la porción en la comida
      PlanFoodPortion portion = PlanFoodPortion.of(planMeal, food, finalQuantity, finalUnit);
      planMeal.addFoodPortion(portion);

      usedFoodIdsInDay.add(food.getId());

      // Deducción del presupuesto restante
      remCal -= addedCal;
      remCarb -= addedCarb;
      remFat -= addedFat;
      remProt -= addedProt;

      // Acumulación consumida
      caloriesConsumed += addedCal;
      carbsConsumed += addedCarb;
      fatsConsumed += addedFat;
      proteinConsumed += addedProt;

      log.info(
          "Meal={} Food={} qty={} unit={} (g={}) | remaining: cal={} carb={} fat={} prot={}",
          planMeal.getType(),
          food.getName(),
          finalQuantity,
          finalUnit,
          actualGrams,
          remCal,
          remCarb,
          remFat,
          remProt);
    }

    int finalCalories =
        (int) Math.min(nutrientBudget.calories().amount(), Math.round(caloriesConsumed));
    int finalCarbs = (int) Math.min(nutrientBudget.carbs().amount(), Math.round(carbsConsumed));
    int finalFat = (int) Math.min(nutrientBudget.fat().amount(), Math.round(fatsConsumed));
    int finalProtein =
        (int) Math.min(nutrientBudget.protein().grams(), Math.round(proteinConsumed));

    return new NutrientBudget(
        new Calories(finalCalories),
        new Carbs(finalCarbs),
        new Fat(finalFat),
        new Protein(finalProtein));
  }

  private double calculateGrams(
      Food food,
      double remCal,
      double remCarb,
      double remFat,
      double remProt,
      double targetCalForSlot,
      double targetCarbForSlot,
      double targetProtForSlot,
      double targetFatForSlot) {

    FoodCategory category = food.getCategory();

    // Solo abortar si el macro principal de la categoría está completamente agotado
    if (remProt <= 1.0 && (category == FoodCategory.PROTEIN || category == FoodCategory.DAIRY)) {
      return ZERO_GRAMS;
    }
    if (remCarb <= 2.0 && (category == FoodCategory.CARB || category == FoodCategory.SWEET)) {
      return ZERO_GRAMS;
    }
    if (remFat <= 1.0 && category == FoodCategory.FAT) {
      return ZERO_GRAMS;
    }

    double calculatedGrams =
        switch (category) {
          case PROTEIN, DAIRY ->
              food.getProteinPer100g() > 0
                  ? (remProt * 100.0) / food.getProteinPer100g()
                  : DEFAULT_MACRO_CALCULATION_GRAMS;

          case CARB, SWEET ->
              food.getCarbsPer100g() > 0
                  ? (remCarb * 100.0) / food.getCarbsPer100g()
                  : DEFAULT_MACRO_CALCULATION_GRAMS;

          case FAT ->
              food.getFatPer100g() > 0
                  ? (remFat * 100.0) / food.getFatPer100g()
                  : DEFAULT_FAT_PORTION_GRAMS;

          case VEGETABLE, FRUIT ->
              food.getDefaultUnit() == MeasureUnit.UNIT
                  ? getUnitWeightInGrams(food)
                  : DEFAULT_FRUIT_VEGETABLE_GRAMS;

          case BEVERAGE -> DEFAULT_BEVERAGE_GRAMS;
        };

    double maximumGrams = calculateMaximumGrams(food, remCal, remCarb, remFat, remProt);
    return Math.min(calculatedGrams, maximumGrams);
  }

  private double clampGramsToFoodLimits(Food food, double calculatedGrams, double remainingCal) {
    double minGrams =
        (food.getMinServingGrams() != null) ? food.getMinServingGrams() : DEFAULT_MIN_SERVING_GRAMS;
    double maxGrams =
        (food.getMaxServingGrams() != null) ? food.getMaxServingGrams() : DEFAULT_MAX_SERVING_GRAMS;

    // Si el mínimo de la porción excede severamente las calorías restantes de la comida, recortar a
    // lo que alcance
    if (food.getCaloriesPer100g() > 0) {
      double maxGramsByCal = (remainingCal * 100.0) / food.getCaloriesPer100g();
      maxGrams = Math.min(maxGrams, maxGramsByCal);
    }

    if (calculatedGrams < minGrams) {
      // Si la cantidad calculada es menor al mínimo pero aún caben calorías, intentar asignar el
      // mínimo
      if (minGrams <= maxGrams) {
        return minGrams;
      }
      return ZERO_GRAMS;
    }

    return Math.min(calculatedGrams, maxGrams);
  }

  private double convertToFinalUnit(Food food, double boundedGrams) {
    MeasureUnit unit = food.getDefaultUnit();

    return switch (unit) {
      case GRAM -> Math.round(boundedGrams);
      case MILLILITER -> {
        double density = getValidDensity(food);
        yield Math.round(boundedGrams / density);
      }
      case UNIT -> {
        double unitWeight = getUnitWeightInGrams(food);
        double exactUnits = boundedGrams / unitWeight;
        // Permite medias unidades (0.5, 1.0, 1.5, etc.) para evitar redondear 0.4 a 1.0 enteros y
        // exceder macros
        double roundedHalfUnits = Math.round(exactUnits * 2.0) / 2.0;
        yield Math.max(0.5, roundedHalfUnits);
      }
    };
  }

  private double calculateActualGrams(Food food, double finalQuantity, MeasureUnit unit) {
    return switch (unit) {
      case UNIT -> finalQuantity * getUnitWeightInGrams(food);
      case MILLILITER -> finalQuantity * getValidDensity(food);
      case GRAM -> finalQuantity;
    };
  }

  private double getValidDensity(Food food) {
    return (food.getDensity() != null && food.getDensity() > 0) ? food.getDensity() : 1.0;
  }

  private double getUnitWeightInGrams(Food food) {
    if (food.getUnitWeight() != null && food.getUnitWeight() > 0) {
      return food.getUnitWeight();
    }
    return 100.0;
  }

  private double calculateMaximumGrams(
      Food food,
      double remainingCal,
      double remainingCarb,
      double remainingFat,
      double remainingProt) {

    double maxGrams = Double.MAX_VALUE;

    if (food.getCaloriesPer100g() > 0 && remainingCal > 0) {
      maxGrams = Math.min(maxGrams, (remainingCal * 100.0) / food.getCaloriesPer100g());
    }

    FoodCategory category = food.getCategory();
    // Solo acotar por el macro principal de la categoría del alimento
    if ((category == FoodCategory.CARB || category == FoodCategory.SWEET)
        && food.getCarbsPer100g() > 0
        && remainingCarb > 0) {
      maxGrams = Math.min(maxGrams, (remainingCarb * 100.0) / food.getCarbsPer100g());
    }

    if ((category == FoodCategory.PROTEIN || category == FoodCategory.DAIRY)
        && food.getProteinPer100g() > 0
        && remainingProt > 0) {
      maxGrams = Math.min(maxGrams, (remainingProt * 100.0) / food.getProteinPer100g());
    }

    if (category == FoodCategory.FAT && food.getFatPer100g() > 0 && remainingFat > 0) {
      maxGrams = Math.min(maxGrams, (remainingFat * 100.0) / food.getFatPer100g());
    }

    return maxGrams;
  }
}
