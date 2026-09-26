package com.lirium.nutrition.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Carbs;
import com.lirium.nutrition.model.valueobject.Fat;
import com.lirium.nutrition.model.valueobject.MacroDeviation;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.model.valueobject.MealAssemblyResult;
import com.lirium.nutrition.model.valueobject.NutrientBudget;
import com.lirium.nutrition.model.valueobject.Protein;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanMealAssemblerImplTest {

  // Daily target: 2000 kcal, 250 g carbs, 70 g fat, 150 g protein
  private static final Calories DAILY_CALORIES = new Calories(2000);
  private static final MacroDistribution DAILY_MACROS = new MacroDistribution(150, 250, 70);

  @Mock private PlanFoodPortionAssembler planFoodPortionAssembler;

  private PlanMealAssemblerImpl planMealAssembler;
  private DailyPlan dailyPlan;
  private PatientProfile patient;

  /** Budgets the meal assembler asked for, in call order (one per meal type). */
  private final List<NutrientBudget> requestedBudgets = new ArrayList<>();

  @BeforeEach
  void setUp() {
    planMealAssembler = new PlanMealAssemblerImpl(planFoodPortionAssembler);
    dailyPlan = DailyPlan.of(DayOfWeek.MONDAY, mock(NutritionPlan.class));
    patient = new PatientProfile(new User());
  }

  @Test
  @DisplayName("Creates the five meals of the day, in order, and adds them to the daily plan")
  void shouldCreateEveryMealTypeInOrder() {
    everyMealConsumesExactlyItsBudget();

    assemble(Set.of());

    assertThat(dailyPlan.getMeals())
        .extracting(PlanMeal::getType)
        .containsExactly(MealType.values());
  }

  @Test
  @DisplayName("The first meal gets its share of the daily target")
  void shouldGiveBreakfastItsShareOfTheDailyTarget() {
    everyMealConsumesExactlyItsBudget();

    assemble(Set.of());

    // Breakfast ratios: 25% calories, 30% carbs, 25% fat, 25% protein
    assertThat(requestedBudgets.getFirst()).isEqualTo(budget(500, 75, 18, 38));
  }

  @Test
  @DisplayName("When every meal hits its target, the whole daily budget is distributed")
  void shouldDistributeTheWholeDailyBudget() {
    everyMealConsumesExactlyItsBudget();

    assemble(Set.of());

    assertThat(requestedBudgets).hasSize(MealType.values().length);
    assertThat(requestedBudgets.stream().mapToInt(b -> b.calories().amount()).sum())
        .isEqualTo(2000);
    assertThat(requestedBudgets.stream().mapToInt(b -> b.carbs().amount()).sum()).isEqualTo(250);
    assertThat(requestedBudgets.stream().mapToInt(b -> b.fat().amount()).sum()).isEqualTo(70);
    assertThat(requestedBudgets.stream().mapToInt(b -> b.protein().grams()).sum()).isEqualTo(150);
  }

  @Test
  @DisplayName("An excess in one meal is discounted from the next one")
  void shouldCarryExcessIntoNextMeal() {
    mealsConsumeTheirBudgetPlus(50, 50);

    assemble(Set.of());

    // After breakfast: 2000 - 550 = 1450 kcal left for 75% of the day's ratios.
    // Mid-morning (10%): 1450 * 0.10 / 0.75 = 193, minus the 50 kcal carried over = 143
    assertThat(requestedBudgets.get(1).calories().amount()).isEqualTo(143);
  }

  @Test
  @DisplayName("A carried-over excess never makes a meal budget negative")
  void shouldNeverProduceNegativeBudgets() {
    mealsConsumeTheirBudgetPlus(5000, 5000);

    assemble(Set.of());

    assertThat(requestedBudgets.subList(1, requestedBudgets.size()))
        .allSatisfy(b -> assertThat(b.calories().amount()).isZero());
  }

  @Test
  @DisplayName("Excludes the tags of the patient's restrictions plus the additional ones")
  void shouldCombinePatientAndAdditionalExcludedTags() {
    patient.addRestriction(restriction(1L, FoodTag.LACTOSE));
    patient.addRestriction(restriction(2L, FoodTag.GLUTEN));
    everyMealConsumesExactlyItsBudget();

    assemble(Set.of(FoodTag.FISH));

    verify(planFoodPortionAssembler, times(MealType.values().length))
        .assemble(
            any(),
            any(),
            eq(Set.of(FoodTag.LACTOSE, FoodTag.GLUTEN, FoodTag.FISH)),
            anySet(),
            anyMap(),
            anyMap());
  }

  @Test
  @DisplayName("Without additional tags, only the patient's restrictions are excluded")
  void shouldUseOnlyPatientRestrictionsByDefault() {
    patient.addRestriction(restriction(1L, FoodTag.LACTOSE));
    everyMealConsumesExactlyItsBudget();

    planMealAssembler.assemble(
        dailyPlan,
        patient,
        DAILY_CALORIES,
        DAILY_MACROS,
        new HashSet<>(),
        new HashMap<>(),
        new HashMap<>());

    verify(planFoodPortionAssembler, times(MealType.values().length))
        .assemble(any(), any(), eq(Set.of(FoodTag.LACTOSE)), anySet(), anyMap(), anyMap());
  }

  // ------------------------------------------------------------ helpers

  private void assemble(Set<FoodTag> additionalExcludedTags) {
    planMealAssembler.assemble(
        dailyPlan,
        patient,
        DAILY_CALORIES,
        DAILY_MACROS,
        additionalExcludedTags,
        new HashSet<>(),
        new HashMap<>(),
        new HashMap<>());
  }

  /** Every meal consumes exactly the budget it receives, with no deviation. */
  private void everyMealConsumesExactlyItsBudget() {
    when(planFoodPortionAssembler.assemble(any(), any(), anySet(), anySet(), anyMap(), anyMap()))
        .thenAnswer(
            invocation -> {
              NutrientBudget budget = invocation.getArgument(1);
              requestedBudgets.add(budget);
              return new MealAssemblyResult(budget, MacroDeviation.ZERO);
            });
  }

  /** Every meal overshoots its calorie budget by the given amount. */
  private void mealsConsumeTheirBudgetPlus(int extraCalories, double deviationCalories) {
    when(planFoodPortionAssembler.assemble(any(), any(), anySet(), anySet(), anyMap(), anyMap()))
        .thenAnswer(
            invocation -> {
              NutrientBudget budget = invocation.getArgument(1);
              requestedBudgets.add(budget);
              NutrientBudget consumed =
                  new NutrientBudget(
                      new Calories(budget.calories().amount() + extraCalories),
                      budget.carbs(),
                      budget.fat(),
                      budget.protein());
              return new MealAssemblyResult(
                  consumed, new MacroDeviation(deviationCalories, 0, 0, 0));
            });
  }

  private static Restriction restriction(Long id, FoodTag excludedTag) {
    return Restriction.builder().id(id).excludedTags(Set.of(excludedTag)).build();
  }

  private static NutrientBudget budget(int calories, int carbs, int fat, int protein) {
    return new NutrientBudget(
        new Calories(calories), new Carbs(carbs), new Fat(fat), new Protein(protein));
  }
}
