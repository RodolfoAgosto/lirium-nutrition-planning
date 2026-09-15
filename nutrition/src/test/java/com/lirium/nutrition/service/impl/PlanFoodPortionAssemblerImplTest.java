package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanFoodPortionAssemblerImplTest {

  @Mock private FoodRepository foodRepository;

  @Mock private PlanFoodPortionAssembler planFoodPortionAssembler;

  @InjectMocks private PlanMealAssemblerImpl planMealAssembler;

  //  @Test
  //  void shouldPassUsedFoodIdsAcrossAllMealsInDay() {
  //    // Given
  //    DailyPlan dailyPlan = mock(DailyPlan.class);
  //    PatientProfile patientProfile = mock(PatientProfile.class);
  //    when(patientProfile.getRestrictions()).thenReturn(Set.of());
  //
  //    // Mockear la respuesta del assembler secundario para evitar que retorne null
  //    NutrientBudget dummyConsumed =
  //        new NutrientBudget(new Calories(0), new Carbs(0), new Fat(0), new Protein(0));
  //    when(planFoodPortionAssembler.assemble(any(), any(), anySet(), anySet()))
  //        .thenReturn(dummyConsumed);
  //
  //    Calories calories = new Calories(2000);
  //    MacroDistribution macros = new MacroDistribution(200, 60, 150);
  //
  //    Set<Long> usedFoodIdsInDay = new HashSet<>();
  //    usedFoodIdsInDay.add(1L);
  //
  //    // When
  //    planMealAssembler.assemble(dailyPlan, patientProfile, calories, macros, usedFoodIdsInDay);
  //
  //    // Then
  //    verify(planFoodPortionAssembler, atLeastOnce())
  //        .assemble(any(), any(), anySet(), eq(usedFoodIdsInDay));
  //  }
  //
  //  @Test
  //  void shouldAssembleLunchWithAllCategories() {
  //    // Given
  //    PlanMeal meal = mock(PlanMeal.class);
  //    when(meal.getType()).thenReturn(MealType.LUNCH);
  //
  //    Food protein = Food.of("Chicken", 150, 30, 0, 5, FoodCategory.PROTEIN,
  // Set.of(MealType.LUNCH));
  //    Food carb = Food.of("Rice", 120, 2, 28, 1, FoodCategory.CARB, Set.of(MealType.LUNCH));
  //    Food vegetable = Food.of("Tomato", 20, 1, 4, 0, FoodCategory.VEGETABLE,
  // Set.of(MealType.LUNCH));
  //    Food sweet = Food.of("Cookie", 400, 4, 70, 10, FoodCategory.SWEET, Set.of(MealType.LUNCH));
  //    Food beverage =
  //        Food.ofLiquid("Juice", 40, 0, 10, 0, FoodCategory.BEVERAGE, Set.of(MealType.LUNCH),
  // 1.0);
  //
  //    when(foodRepository.findSuitableFoods(eq(MealType.LUNCH), anySet()))
  //        .thenReturn(new ArrayList<>(List.of(protein, carb, vegetable, sweet, beverage)));
  //
  //    PlanFoodPortionAssemblerImpl assembler = new PlanFoodPortionAssemblerImpl(foodRepository);
  //
  //    NutrientBudget budget =
  //        new NutrientBudget(new Calories(700), new Carbs(80), new Fat(20), new Protein(50));
  //
  //    Set<Long> usedFoodIdsInDay = new HashSet<>();
  //
  //    // When - 3 parámetros: (planMeal, budget, usedFoodIdsInDay)
  //    assembler.assemble(meal, budget, usedFoodIdsInDay);
  //
  //    // Then
  //    verify(meal, atLeast(3)).addFoodPortion(any());
  //  }

  //  @Test
  //  void shouldMergeRestrictionTagsAndAdditionalExcludedTags() {
  //    // Given
  //    PlanMeal meal = mock(PlanMeal.class);
  //    when(meal.getType()).thenReturn(MealType.LUNCH);
  //
  //    Set<FoodTag> excludedTags = Set.of(FoodTag.GLUTEN, FoodTag.HONEY);
  //
  //    Food protein = Food.of("Chicken", 150, 30, 0, 5, FoodCategory.PROTEIN,
  // Set.of(MealType.LUNCH));
  //    Food carb = Food.of("Rice", 120, 2, 28, 1, FoodCategory.CARB, Set.of(MealType.LUNCH));
  //
  //    when(foodRepository.findSuitableFoods(eq(MealType.LUNCH), anySet()))
  //        .thenReturn(new ArrayList<>(List.of(protein, carb)));
  //
  //    PlanFoodPortionAssemblerImpl assembler = new PlanFoodPortionAssemblerImpl(foodRepository);
  //
  //    NutrientBudget budget =
  //        new NutrientBudget(new Calories(700), new Carbs(80), new Fat(20), new Protein(50));
  //
  //    Set<Long> usedFoodIdsInDay = new HashSet<>();
  //
  //    // When - 4 parámetros: (planMeal, budget, excludedTags, usedFoodIdsInDay)
  //    assembler.assemble(meal, budget, excludedTags, usedFoodIdsInDay);
  //
  //    // Then
  //    @SuppressWarnings("unchecked")
  //    ArgumentCaptor<Set<FoodTag>> captor = ArgumentCaptor.forClass(Set.class);
  //
  //    verify(foodRepository).findSuitableFoods(eq(MealType.LUNCH), captor.capture());
  //
  //    Set<FoodTag> tags = captor.getValue();
  //    assertTrue(tags.contains(FoodTag.GLUTEN));
  //    assertTrue(tags.contains(FoodTag.HONEY));
  //  }
}
