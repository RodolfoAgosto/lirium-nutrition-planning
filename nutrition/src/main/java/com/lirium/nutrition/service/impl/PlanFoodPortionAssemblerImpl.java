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
  private final Random random = new Random();

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
  public MealAssemblyResult assemble(
      PlanMeal planMeal,
      NutrientBudget target,
      Set<Long> usedFoodIdsInDay,
      Map<Long, Integer> foodFrequencyInWeek,
      Map<Long, Double> foodGramsInDay) {
    return assemble(
        planMeal,
        target,
        Collections.emptySet(),
        usedFoodIdsInDay,
        foodFrequencyInWeek,
        foodGramsInDay);
  }

  @Override
  public MealAssemblyResult assemble(
      PlanMeal planMeal,
      NutrientBudget target,
      Set<FoodTag> excludedTags,
      Set<Long> usedFoodIdsInDay,
      Map<Long, Integer> foodFrequencyInWeek,
      Map<Long, Double> foodGramsInDay) {

    double caloriesConsumed = 0.0;
    double carbsConsumed = 0.0;
    double fatsConsumed = 0.0;
    double proteinConsumed = 0.0;

    List<Food> availableFoods =
        new ArrayList<>(foodRepository.findSuitableFoods(planMeal.getType(), excludedTags));
    Collections.shuffle(availableFoods);

    List<SlotDistribution> slots =
        new ArrayList<>(distributions.getOrDefault(planMeal.getType(), Collections.emptyList()));

    double remCal = target.calories().amount();
    double remCarb = target.carbs().amount();
    double remFat = target.fat().amount();
    double remProt = target.protein().grams();

    for (SlotDistribution slot : slots) {

      if (remCal <= 10.0 && remProt <= 2.0) {
        break;
      }

      final double currentRemCal = remCal;
      final double currentRemCarb = remCarb;
      final double currentRemFat = remFat;
      final double currentRemProt = remProt;

      List<Food> candidates =
          availableFoods.stream()
              .filter(f -> f.getCategory() == slot.category())
              .filter(f -> !usedFoodIdsInDay.contains(f.getId()))
              .filter(f -> foodFrequencyInWeek.getOrDefault(f.getId(), 0) < getMaxFrequency(f))
              .filter(
                  f -> {
                    double estimatedGrams =
                        estimateGrams(
                            f,
                            currentRemCal,
                            currentRemCarb,
                            currentRemFat,
                            currentRemProt,
                            slot.category());
                    if (estimatedGrams <= 0) return false;
                    return !isExtremeDeviation(
                        f,
                        estimatedGrams,
                        currentRemCal,
                        currentRemCarb,
                        currentRemFat,
                        currentRemProt);
                  })
              .sorted(
                  Comparator.comparingDouble(
                      f ->
                          calculateDeviationScore(
                              f,
                              currentRemCal,
                              currentRemCarb,
                              currentRemFat,
                              currentRemProt,
                              slot.category())))
              .limit(3)
              .toList();

      Optional<Food> foodOpt =
          candidates.isEmpty()
              ? Optional.empty()
              : Optional.of(candidates.get(random.nextInt(candidates.size())));

      // Fallback (respeta frecuencia; matchesSlotOrFallback también acepta legumbres para el slot
      // PROTEIN)
      if (foodOpt.isEmpty()) {
        foodOpt =
            availableFoods.stream()
                .filter(f -> matchesSlotOrFallback(f, slot.category()))
                .filter(f -> !usedFoodIdsInDay.contains(f.getId()))
                .filter(f -> foodFrequencyInWeek.getOrDefault(f.getId(), 0) < getMaxFrequency(f))
                .findFirst();
      }

      if (foodOpt.isEmpty()) {
        log.warn("No food found for category={} in meal={}", slot.category(), planMeal.getType());
        continue;
      }

      Food food = foodOpt.get();
      availableFoods.remove(food);

      // 1. Calcular gramos teóricos basándose en la responsabilidad del slot y remanentes
      double rawGrams = calculateGrams(food, slot.category(), remCal, remCarb, remFat, remProt);
      if (rawGrams <= 0) {
        continue;
      }

      // 2. Acotar según límites de porción del alimento y calorías restantes
      double boundedGrams = clampGramsToFoodLimits(food, rawGrams, remCal);
      boundedGrams = adjustGramsForFatTolerance(food, boundedGrams, remFat);
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

      // También evitamos pasarnos mucho de calorías
      if (addedCal > remCal + 40.0 && food.getCategory() != FoodCategory.VEGETABLE) {
        continue;
      }

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
      foodFrequencyInWeek.merge(food.getId(), 1, Integer::sum);
      foodGramsInDay.merge(food.getId(), actualGrams, Double::sum);

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

    int topUpAttempts = 0;
    while (remProt > 15.0 && remCal > 50.0 && topUpAttempts < 2) {
      topUpAttempts++;

      Optional<Food> topUpFood =
          availableFoods.stream()
              .filter(
                  f ->
                      f.getCategory() == FoodCategory.PROTEIN
                          || f.getCategory() == FoodCategory.DAIRY)
              .filter(f -> !usedFoodIdsInDay.contains(f.getId()))
              .filter(f -> foodFrequencyInWeek.getOrDefault(f.getId(), 0) < getMaxFrequency(f))
              .max(Comparator.comparingDouble(Food::getProteinPer100g));

      if (topUpFood.isEmpty()) break;

      Food food = topUpFood.get();
      availableFoods.remove(food);

      double rawGrams =
          calculateGrams(food, FoodCategory.PROTEIN, remCal, remCarb, remFat, remProt);
      double boundedGrams = clampGramsToFoodLimits(food, rawGrams, remCal);
      boundedGrams = adjustGramsForFatTolerance(food, boundedGrams, remFat);
      if (boundedGrams <= 0) continue;

      double finalQuantity = convertToFinalUnit(food, boundedGrams);
      MeasureUnit finalUnit = food.getDefaultUnit();
      double actualGrams = calculateActualGrams(food, finalQuantity, finalUnit);

      double addedCal = (food.getCaloriesPer100g() * actualGrams) / 100.0;
      double addedCarb = (food.getCarbsPer100g() * actualGrams) / 100.0;
      double addedFat = (food.getFatPer100g() * actualGrams) / 100.0;
      double addedProt = (food.getProteinPer100g() * actualGrams) / 100.0;

      if (addedCal > remCal + 50.0) continue;

      PlanFoodPortion portion = PlanFoodPortion.of(planMeal, food, finalQuantity, finalUnit);
      planMeal.addFoodPortion(portion);

      usedFoodIdsInDay.add(food.getId());
      foodFrequencyInWeek.merge(food.getId(), 1, Integer::sum);
      foodGramsInDay.merge(food.getId(), actualGrams, Double::sum);

      remCal -= addedCal;
      remCarb -= addedCarb;
      remFat -= addedFat;
      remProt -= addedProt;
      caloriesConsumed += addedCal;
      carbsConsumed += addedCarb;
      fatsConsumed += addedFat;
      proteinConsumed += addedProt;
    }

    int finalCalories = (int) Math.round(caloriesConsumed);
    int finalCarbs = (int) Math.round(carbsConsumed);
    int finalFat = (int) Math.round(fatsConsumed);
    int finalProtein = (int) Math.round(proteinConsumed);

    NutrientBudget consumed =
        new NutrientBudget(
            new Calories(finalCalories),
            new Carbs(finalCarbs),
            new Fat(finalFat),
            new Protein(finalProtein));

    MacroDeviation deviation =
        new MacroDeviation(
            (double) finalCalories - target.calories().amount(),
            (double) finalCarbs - target.carbs().amount(),
            (double) finalFat - target.fat().amount(),
            (double) finalProtein - target.protein().grams());

    return new MealAssemblyResult(consumed, deviation);
  }

  private double calculateGrams(
      Food food,
      FoodCategory slotCategory,
      double remCal,
      double remCarb,
      double remFat,
      double remProt) {

    FoodCategory effectiveCategory = resolveEffectiveCategory(food, slotCategory);

    if (remProt <= 1.0
        && (effectiveCategory == FoodCategory.PROTEIN || effectiveCategory == FoodCategory.DAIRY)) {
      return ZERO_GRAMS;
    }
    if (remCarb <= 2.0
        && (effectiveCategory == FoodCategory.CARB || effectiveCategory == FoodCategory.SWEET)) {
      return ZERO_GRAMS;
    }
    if (remFat <= 1.0 && effectiveCategory == FoodCategory.FAT) {
      return ZERO_GRAMS;
    }

    double calculatedGrams =
        switch (effectiveCategory) {
          case PROTEIN, DAIRY ->
              food.getProteinPer100g() > 0
                  ? (Math.max(0.0, remProt) * 100.0) / food.getProteinPer100g()
                  : DEFAULT_MACRO_CALCULATION_GRAMS;

          case CARB, SWEET ->
              food.getCarbsPer100g() > 0
                  ? (Math.max(0.0, remCarb) * 100.0) / food.getCarbsPer100g()
                  : DEFAULT_MACRO_CALCULATION_GRAMS;

          case FAT ->
              food.getFatPer100g() > 0
                  ? (Math.max(0.0, remFat) * 100.0) / food.getFatPer100g()
                  : DEFAULT_FAT_PORTION_GRAMS;

          case VEGETABLE, FRUIT ->
              food.getDefaultUnit() == MeasureUnit.UNIT
                  ? getUnitWeightInGrams(food)
                  : DEFAULT_FRUIT_VEGETABLE_GRAMS;

          case BEVERAGE -> DEFAULT_BEVERAGE_GRAMS;
        };

    double maximumGrams =
        calculateMaximumGrams(food, effectiveCategory, remCal, remCarb, remFat, remProt);
    return Math.min(calculatedGrams, maximumGrams);
  }

  private FoodCategory resolveEffectiveCategory(Food food, FoodCategory slotCategory) {
    boolean legumeFillingProteinSlot =
        slotCategory == FoodCategory.PROTEIN
            && food.getCategory() != FoodCategory.PROTEIN
            && food.getCategory() != FoodCategory.DAIRY
            && food.getFoodTags().contains(FoodTag.LEGUME);

    return legumeFillingProteinSlot ? FoodCategory.PROTEIN : food.getCategory();
  }

  private double clampGramsToFoodLimits(Food food, double calculatedGrams, double remainingCal) {
    double minGrams =
        (food.getMinServingGrams() != null) ? food.getMinServingGrams() : DEFAULT_MIN_SERVING_GRAMS;
    double maxGrams =
        (food.getMaxServingGrams() != null) ? food.getMaxServingGrams() : DEFAULT_MAX_SERVING_GRAMS;

    if (food.getCaloriesPer100g() > 0) {
      double maxGramsByCal = (remainingCal * 100.0) / food.getCaloriesPer100g();
      maxGrams = Math.min(maxGrams, maxGramsByCal);
    }

    // Si ni siquiera entra la porción mínima razonable, no lo agregues
    if (maxGrams < minGrams) {
      return ZERO_GRAMS;
    }

    if (calculatedGrams < minGrams) {
      return minGrams;
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
      FoodCategory effectiveCategory,
      double remainingCal,
      double remainingCarb,
      double remainingFat,
      double remainingProt) {

    double maxGrams = Double.MAX_VALUE;

    if (food.getCaloriesPer100g() > 0 && remainingCal > 0) {
      maxGrams = Math.min(maxGrams, (remainingCal * 100.0) / food.getCaloriesPer100g());
    }

    if ((effectiveCategory == FoodCategory.CARB || effectiveCategory == FoodCategory.SWEET)
        && food.getCarbsPer100g() > 0
        && remainingCarb > 0) {
      maxGrams = Math.min(maxGrams, (remainingCarb * 100.0) / food.getCarbsPer100g());
    }

    if ((effectiveCategory == FoodCategory.PROTEIN || effectiveCategory == FoodCategory.DAIRY)
        && food.getProteinPer100g() > 0
        && remainingProt > 0) {
      maxGrams = Math.min(maxGrams, (remainingProt * 100.0) / food.getProteinPer100g());
    }

    if (food.getFatPer100g() > 0
        && remainingFat > 0
        && effectiveCategory != FoodCategory.PROTEIN
        && effectiveCategory != FoodCategory.DAIRY) {
      double maxAllowedFat = remainingFat + 4.0;
      maxGrams = Math.min(maxGrams, (maxAllowedFat * 100.0) / food.getFatPer100g());
    }

    return maxGrams;
  }

  private int getMaxFrequency(Food food) {
    // Podés hacerlo más fino por categoría o por food específico
    if (food.getCategory() == FoodCategory.DAIRY) {
      return 2; // máximo 2 veces por día (o por semana si el mapa es semanal)
    }
    return 4; // resto de alimentos
  }

  private double estimateGrams(
      Food food,
      double remCal,
      double remCarb,
      double remFat,
      double remProt,
      FoodCategory slotCategory) {
    return calculateGrams(food, slotCategory, remCal, remCarb, remFat, remProt);
  }

  private boolean isExtremeDeviation(
      Food food, double grams, double remCal, double remCarb, double remFat, double remProt) {

    double addedCal = (food.getCaloriesPer100g() * grams) / 100.0;
    double addedFat = (food.getFatPer100g() * grams) / 100.0;
    double addedProt = (food.getProteinPer100g() * grams) / 100.0;

    double calRatio = remCal > 0 ? addedCal / remCal : 0;
    double fatRatio = remFat > 0 ? addedFat / remFat : 0;
    double protRatio = remProt > 0 ? addedProt / remProt : 0;

    // Rechazar si se pasa mucho de calorías, grasa o proteína
    return calRatio > 1.4 || fatRatio > 1.3 || protRatio > 1.5;
  }

  private double calculateDeviationScore(
      Food food,
      double remCal,
      double remCarb,
      double remFat,
      double remProt,
      FoodCategory slotCategory) {

    double grams = estimateGrams(food, remCal, remCarb, remFat, remProt, slotCategory);
    if (grams <= 0) return Double.MAX_VALUE;

    double addedCal = (food.getCaloriesPer100g() * grams) / 100.0;
    double addedCarb = (food.getCarbsPer100g() * grams) / 100.0;
    double addedFat = (food.getFatPer100g() * grams) / 100.0;
    double addedProt = (food.getProteinPer100g() * grams) / 100.0;

    // Normalizamos para que no domine un macro
    double calDev =
        remCal > 0 ? Math.abs(addedCal - remCal * 0.45) / remCal : 0; // 0.45 ≈ proporción del slot
    double carbDev = remCarb > 0 ? Math.abs(addedCarb - remCarb * 0.4) / remCarb : 0;
    double fatDev = remFat > 0 ? Math.abs(addedFat - remFat * 0.3) / remFat : 0;
    double protDev = remProt > 0 ? Math.abs(addedProt - remProt * 0.4) / remProt : 0;

    // Penalizamos la grasa si vamos altos
    return calDev * 1.2 + carbDev * 1.1 + fatDev * 2.0 + protDev * 2.5;
  }

  private double adjustGramsForFatTolerance(Food food, double grams, double remainingFat) {

    double addedFat = (food.getFatPer100g() * grams) / 100.0;
    double maxAllowedFat = remainingFat + 4.0;

    if (addedFat > maxAllowedFat && addedFat > 0) {
      return grams * (maxAllowedFat / addedFat);
    }

    return grams;
  }

  private boolean matchesSlotOrFallback(Food food, FoodCategory slotCategory) {
    if (food.getCategory() == slotCategory) {
      return true;
    }
    // Fallback: Permitir legumbres (CARB + tag LEGUME) en el slot PROTEIN
    return slotCategory == FoodCategory.PROTEIN
        && food.getCategory() == FoodCategory.CARB
        && food.getFoodTags() != null
        && food.getFoodTags().contains(FoodTag.LEGUME);
  }
}
