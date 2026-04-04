package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import static com.lirium.nutrition.model.enums.MeasureUnit.MILLILITER;

@Service
public class PlanFoodPortionAssemblerImpl implements PlanFoodPortionAssembler {

    private final Map<MealType, List<SlotDistribution>> distributions;

    private  final FoodRepository foodRepository;

    public PlanFoodPortionAssemblerImpl(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
        this.distributions = buildDistributions();
    }

    private Map<MealType, List<SlotDistribution>> buildDistributions() {
        return Map.of(
                MealType.BREAKFAST, List.of(
                        new SlotDistribution(FoodCategory.DAIRY),
                        new SlotDistribution(FoodCategory.CARB),
                        new SlotDistribution(FoodCategory.BEVERAGE)
                ),
                MealType.LUNCH, List.of(
                        new SlotDistribution(FoodCategory.PROTEIN),
                        new SlotDistribution(FoodCategory.CARB),
                        new SlotDistribution(FoodCategory.VEGETABLE),
                        new SlotDistribution(FoodCategory.SWEET),
                        new SlotDistribution(FoodCategory.BEVERAGE)
                ),
                MealType.MID_MORNING, List.of(
                        new SlotDistribution(FoodCategory.DAIRY),
                        new SlotDistribution(FoodCategory.FRUIT)
                ),
                MealType.SNACK, List.of(
                        new SlotDistribution(FoodCategory.DAIRY),
                        new SlotDistribution(FoodCategory.CARB),
                        new SlotDistribution(FoodCategory.BEVERAGE)
                ),
                MealType.DINNER, List.of(
                        new SlotDistribution(FoodCategory.PROTEIN),
                        new SlotDistribution(FoodCategory.CARB),
                        new SlotDistribution(FoodCategory.VEGETABLE),
                        new SlotDistribution(FoodCategory.FRUIT),
                        new SlotDistribution(FoodCategory.BEVERAGE)
                )
        );
    }

    record SlotDistribution(FoodCategory category) {}

    @Override
    public void assemble(PlanMeal planMeal, PatientProfile patient, Calories calories, Fat fat, Carbs carbs, Protein protein) {
        assemble(planMeal, patient, Collections.emptySet(), calories, fat, carbs, protein);
    }

    @Override
    public void assemble(PlanMeal planMeal, PatientProfile patient, Set<FoodTag> additionalExcludedTags, Calories calories, Fat fat, Carbs carbs, Protein protein) {
        // Restricciones (tags): SQL — es un filtro de exclusión simple
        // Tipo de comida: SQL — food_suitable_for ya existe
        List<Food> foods;
        Set<FoodTag> excludedTags = new HashSet<>(resolveExcludedTags(patient.getRestrictions()));
        excludedTags.addAll(additionalExcludedTags);
        foods = foodRepository.findSuitableFoods(planMeal.getType(), excludedTags);
        Collections.shuffle(foods);

        // Macronutrientes: Aplicación — requiere cálculo y ajuste
        List<SlotDistribution> slots = new ArrayList<>(distributions.get(planMeal.getType()));
        if (planMeal.getType() == MealType.MID_MORNING) {
            Collections.shuffle(slots);
            slots = List.of(slots.get(0));
        }

        // Restante acumulativo - arranca con el objetivo completo
        double remCal  = calories.amount();
        double remCarb = carbs.amount();
        double remFat  = fat.amount();
        double remProt = protein.grams();

        for (int i = 0; i < slots.size(); i++) {
            SlotDistribution slot = slots.get(i);

            Food food = foods.stream()
                    .filter(f -> f.getCategory() == slot.category())
                    .findFirst()
                    .orElseThrow();

            double grams = calculateGrams(food, remCal, remCarb, remFat, remProt);

            PlanFoodPortion portion = PlanFoodPortion.of(planMeal, food, grams, food.getDefaultUnit());
            planMeal.addFoodPortion(portion);

            double actualGrams = portion.grams();

            remCal  -= food.getCaloriesPer100g()  * actualGrams / 100;
            remCarb -= food.getCarbsPer100g()     * actualGrams / 100;
            remFat  -= food.getFatPer100g()       * actualGrams / 100;
            remProt -= food.getProteinPer100g()   * actualGrams / 100;

        }

    }

    private double calculateGrams(Food food, double targetCal, double targetCarb, double targetFat, double targetProt) {

        double grams = switch (food.getCategory()) {
            case PROTEIN, DAIRY -> food.getProteinPer100g() > 0
                    ? targetProt * 100 / food.getProteinPer100g()
                    : 100.0; // porción default si el food no tiene proteína definida
            case CARB, SWEET -> food.getCarbsPer100g() > 0
                    ? targetCarb * 100 / food.getCarbsPer100g()
                    : 100.0;
            case FAT -> food.getFatPer100g() > 0
                    ? targetFat * 100 / food.getFatPer100g()
                    : 15.0;
            case VEGETABLE, FRUIT -> food.getDefaultUnit() == MeasureUnit.UNIT ? 1.0 : 150.0;
            case BEVERAGE       -> food.toGrams(200.0, MILLILITER);
        };
        return Math.max(grams, 1.0);

    }

    private Set<FoodTag> resolveExcludedTags(Set<Restriction> restrictions){

        return restrictions.stream()
                .flatMap(r -> r.getExcludedTags().stream())
                .collect(Collectors.toSet());

    }

}
