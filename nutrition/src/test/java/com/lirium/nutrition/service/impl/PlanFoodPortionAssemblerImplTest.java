package com.lirium.nutrition.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Carbs;
import com.lirium.nutrition.model.valueobject.Fat;
import com.lirium.nutrition.model.valueobject.MealAssemblyResult;
import com.lirium.nutrition.model.valueobject.NutrientBudget;
import com.lirium.nutrition.model.valueobject.Protein;
import com.lirium.nutrition.repository.FoodRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The assembler picks foods at random among the best candidates of each slot. To keep every test
 * deterministic, each scenario offers at most one food per category (the top-up test is the only
 * one with two candidates, and its assertions hold for either order).
 */
@ExtendWith(MockitoExtension.class)
class PlanFoodPortionAssemblerImplTest {

  private static final NutrientBudget STANDARD_TARGET = budget(600, 70, 20, 35);

  @Mock private FoodRepository foodRepository;

  private PlanFoodPortionAssemblerImpl assembler;

  private Set<Long> usedFoodIdsInDay;
  private Map<Long, Integer> foodFrequencyInWeek;
  private Map<Long, Double> foodGramsInDay;

  @BeforeEach
  void setUp() {
    assembler = new PlanFoodPortionAssemblerImpl(foodRepository);
    usedFoodIdsInDay = new HashSet<>();
    foodFrequencyInWeek = new HashMap<>();
    foodGramsInDay = new HashMap<>();
  }

  // ---------------------------------------------------------------- slots

  @Test
  @DisplayName("Fills every breakfast slot and records the foods used in the day and the week")
  void shouldFillEveryBreakfastSlot() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    Food bread = food(2L, "Bread", 250, 8, 50, 2, FoodCategory.CARB);
    Food tea = food(3L, "Tea", 1, 0, 0, 0, FoodCategory.BEVERAGE);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt, bread, tea);

    MealAssemblyResult result = assemble(breakfast, STANDARD_TARGET);

    assertThat(foodNames(breakfast)).containsExactlyInAnyOrder("Yogurt", "Bread", "Tea");
    assertThat(usedFoodIdsInDay).containsExactlyInAnyOrder(1L, 2L, 3L);
    assertThat(foodFrequencyInWeek).containsEntry(1L, 1).containsEntry(2L, 1).containsEntry(3L, 1);
    assertThat(foodGramsInDay).containsKeys(1L, 2L, 3L);
    assertThat(result.consumed().calories().amount()).isPositive();
  }

  @Test
  @DisplayName("The deviation is what was consumed minus the target, for every macro")
  void shouldReportDeviationAsConsumedMinusTarget() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    Food bread = food(2L, "Bread", 250, 8, 50, 2, FoodCategory.CARB);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt, bread);

    MealAssemblyResult result = assemble(breakfast, STANDARD_TARGET);

    NutrientBudget consumed = result.consumed();
    assertThat(result.deviation().calories()).isEqualTo(consumed.calories().amount() - 600.0);
    assertThat(result.deviation().carbs()).isEqualTo(consumed.carbs().amount() - 70.0);
    assertThat(result.deviation().fat()).isEqualTo(consumed.fat().amount() - 20.0);
    assertThat(result.deviation().protein()).isEqualTo(consumed.protein().grams() - 35.0);
  }

  @Test
  @DisplayName("A legume can fill the protein slot when there is no protein food")
  void shouldUseLegumeForProteinSlot() {
    Food lentils = food(1L, "Lentils", 116, 9, 20, 0, FoodCategory.CARB);
    lentils.addTag(FoodTag.LEGUME);
    PlanMeal lunch = meal(MealType.LUNCH, lentils);

    assemble(lunch, STANDARD_TARGET);

    assertThat(foodNames(lunch)).containsExactly("Lentils");
  }

  @Test
  @DisplayName("Tops up protein with a second protein food when the target is still far")
  void shouldTopUpProteinWhenStillFarFromTarget() {
    Food chicken = food(1L, "Chicken", 165, 31, 0, 4, FoodCategory.PROTEIN);
    Food tuna = food(2L, "Tuna", 130, 26, 0, 1, FoodCategory.PROTEIN);
    PlanMeal lunch = meal(MealType.LUNCH, chicken, tuna);

    assemble(lunch, budget(1500, 150, 50, 120));

    assertThat(foodNames(lunch)).containsExactlyInAnyOrder("Chicken", "Tuna");
  }

  // ------------------------------------------------------ variety rules

  @Test
  @DisplayName("A food already used today is not repeated")
  void shouldNotRepeatFoodUsedEarlierInTheDay() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt);
    usedFoodIdsInDay.add(1L);

    MealAssemblyResult result = assemble(breakfast, STANDARD_TARGET);

    assertThat(breakfast.getFoodPortions()).isEmpty();
    assertThat(result.consumed().calories().amount()).isZero();
    assertThat(result.deviation().calories()).isEqualTo(-600.0);
  }

  @Test
  @DisplayName("Dairy is limited to 2 appearances per week")
  void shouldRespectWeeklyLimitForDairy() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt);
    foodFrequencyInWeek.put(1L, 2);

    assemble(breakfast, STANDARD_TARGET);

    assertThat(breakfast.getFoodPortions()).isEmpty();
  }

  @Test
  @DisplayName("Other foods can appear up to 4 times per week")
  void shouldAllowNonDairyFoodBelowWeeklyLimit() {
    Food bread = food(1L, "Bread", 250, 8, 50, 2, FoodCategory.CARB);
    PlanMeal breakfast = meal(MealType.BREAKFAST, bread);
    foodFrequencyInWeek.put(1L, 3);

    assemble(breakfast, STANDARD_TARGET);

    assertThat(foodNames(breakfast)).containsExactly("Bread");
    assertThat(foodFrequencyInWeek).containsEntry(1L, 4);
  }

  @Test
  @DisplayName("A non-dairy food that reached 4 appearances this week is skipped")
  void shouldSkipNonDairyFoodAtWeeklyLimit() {
    Food bread = food(1L, "Bread", 250, 8, 50, 2, FoodCategory.CARB);
    PlanMeal breakfast = meal(MealType.BREAKFAST, bread);
    foodFrequencyInWeek.put(1L, 4);

    assemble(breakfast, STANDARD_TARGET);

    assertThat(breakfast.getFoodPortions()).isEmpty();
  }

  // ------------------------------------------------------ budget limits

  @Test
  @DisplayName("Adds nothing when the calorie and protein budget is already spent")
  void shouldAddNothingWhenBudgetIsSpent() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt);

    MealAssemblyResult result = assemble(breakfast, budget(10, 0, 0, 2));

    assertThat(breakfast.getFoodPortions()).isEmpty();
    assertThat(result.consumed().calories().amount()).isZero();
  }

  @Test
  @DisplayName("Skips a carb food when there are no carbs left in the budget")
  void shouldSkipCarbFoodWhenNoCarbsLeft() {
    Food bread = food(1L, "Bread", 250, 8, 50, 2, FoodCategory.CARB);
    PlanMeal breakfast = meal(MealType.BREAKFAST, bread);

    assemble(breakfast, budget(600, 1, 20, 35));

    assertThat(breakfast.getFoodPortions()).isEmpty();
  }

  @Test
  @DisplayName("Skips a food when not even its minimum serving fits the remaining calories")
  void shouldSkipFoodWhenMinimumServingDoesNotFit() {
    Food cheese = food(1L, "Cheese", 400, 25, 1, 33, FoodCategory.DAIRY);
    cheese.changeServingLimits(30.0, 100.0);
    PlanMeal breakfast = meal(MealType.BREAKFAST, cheese);

    assemble(breakfast, budget(30, 5, 5, 20));

    assertThat(breakfast.getFoodPortions()).isEmpty();
  }

  // ------------------------------------------------------ portion sizes

  @Test
  @DisplayName("Never serves less than the food's minimum serving")
  void shouldApplyMinimumServing() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    yogurt.changeServingLimits(150.0, 300.0);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt);

    assemble(breakfast, budget(600, 70, 20, 5));

    assertThat(onlyPortion(breakfast).getQuantity()).isEqualTo(150.0);
  }

  @Test
  @DisplayName("Never serves more than the default maximum of 300 g")
  void shouldCapPortionAtDefaultMaximum() {
    Food yogurt = food(1L, "Yogurt", 60, 10, 4, 1, FoodCategory.DAIRY);
    PlanMeal breakfast = meal(MealType.BREAKFAST, yogurt);

    assemble(breakfast, STANDARD_TARGET);

    assertThat(onlyPortion(breakfast).getQuantity()).isEqualTo(300.0);
  }

  @Test
  @DisplayName("Reduces a fat portion so it doesn't exceed the fat budget by more than 4 g")
  void shouldReduceFatPortionToFatTolerance() {
    Food oil = food(1L, "Olive oil", 884, 0, 0, 100, FoodCategory.FAT);
    PlanMeal lunch = meal(MealType.LUNCH, oil);

    assemble(lunch, budget(600, 70, 5, 35));

    // The 10 g default minimum would add 10 g of fat; the tolerance is 5 + 4 = 9 g
    assertThat(onlyPortion(lunch).getQuantity()).isEqualTo(9.0);
  }

  @Test
  @DisplayName("Foods measured in units are served in whole or half units")
  void shouldServeUnitFoodsInHalfUnits() {
    Food apple = Food.ofUnit("Apple", 52, 0, 14, 0, FoodCategory.FRUIT, Set.of(), 180.0);
    ReflectionTestUtils.setField(apple, "id", 1L);
    PlanMeal midMorning = meal(MealType.MID_MORNING, apple);

    assemble(midMorning, STANDARD_TARGET);

    PlanFoodPortion portion = onlyPortion(midMorning);
    assertThat(portion.getMeasureUnit()).isEqualTo(MeasureUnit.UNIT);
    assertThat(portion.getQuantity()).isEqualTo(1.0);
  }

  @Test
  @DisplayName("Liquids are served in milliliters using the food's density")
  void shouldServeLiquidsInMilliliters() {
    Food juice = Food.ofLiquid("Orange juice", 45, 1, 10, 0, FoodCategory.BEVERAGE, Set.of(), 1.05);
    ReflectionTestUtils.setField(juice, "id", 1L);
    PlanMeal breakfast = meal(MealType.BREAKFAST, juice);

    assemble(breakfast, STANDARD_TARGET);

    PlanFoodPortion portion = onlyPortion(breakfast);
    assertThat(portion.getMeasureUnit()).isEqualTo(MeasureUnit.MILLILITER);
    assertThat(portion.getQuantity()).isEqualTo(190.0); // 200 g / 1.05 density, rounded
    assertThat(foodGramsInDay.get(1L)).isEqualTo(190 * 1.05);
  }

  // ------------------------------------------------------ repository call

  @Test
  @DisplayName("Asks the repository only for foods without the excluded tags")
  void shouldPassExcludedTagsToRepository() {
    PlanMeal lunch = PlanMeal.of(MealType.LUNCH, mock(DailyPlan.class));
    when(foodRepository.findSuitableFoods(any(), any())).thenReturn(List.of());

    assembler.assemble(
        lunch,
        STANDARD_TARGET,
        Set.of(FoodTag.GLUTEN),
        usedFoodIdsInDay,
        foodFrequencyInWeek,
        foodGramsInDay);

    verify(foodRepository).findSuitableFoods(MealType.LUNCH, Set.of(FoodTag.GLUTEN));
  }

  @Test
  @DisplayName("Without excluded tags, the repository is asked with an empty set")
  void shouldUseEmptyExcludedTagsByDefault() {
    PlanMeal lunch = PlanMeal.of(MealType.LUNCH, mock(DailyPlan.class));
    when(foodRepository.findSuitableFoods(any(), any())).thenReturn(List.of());

    assembler.assemble(
        lunch, STANDARD_TARGET, usedFoodIdsInDay, foodFrequencyInWeek, foodGramsInDay);

    verify(foodRepository).findSuitableFoods(eq(MealType.LUNCH), eq(Set.of()));
  }

  // ------------------------------------------------------------ helpers

  private MealAssemblyResult assemble(PlanMeal meal, NutrientBudget target) {
    return assembler.assemble(
        meal, target, Set.of(), usedFoodIdsInDay, foodFrequencyInWeek, foodGramsInDay);
  }

  private PlanMeal meal(MealType type, Food... availableFoods) {
    when(foodRepository.findSuitableFoods(eq(type), any())).thenReturn(List.of(availableFoods));
    return PlanMeal.of(type, mock(DailyPlan.class));
  }

  private static Food food(
      Long id, String name, int calories, int protein, int carbs, int fat, FoodCategory category) {
    Food food = Food.of(name, calories, protein, carbs, fat, category, Set.of());
    ReflectionTestUtils.setField(food, "id", id);
    return food;
  }

  private static NutrientBudget budget(int calories, int carbs, int fat, int protein) {
    return new NutrientBudget(
        new Calories(calories), new Carbs(carbs), new Fat(fat), new Protein(protein));
  }

  private static List<String> foodNames(PlanMeal meal) {
    return meal.getFoodPortions().stream().map(p -> p.getFood().getName()).toList();
  }

  private static PlanFoodPortion onlyPortion(PlanMeal meal) {
    assertThat(meal.getFoodPortions()).hasSize(1);
    return meal.getFoodPortions().getFirst();
  }
}
