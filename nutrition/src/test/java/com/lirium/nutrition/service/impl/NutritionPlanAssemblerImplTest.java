package com.lirium.nutrition.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionPlanAssemblerImplTest {

  //  @Test
  //  void shouldAssembleNutritionPlanSuccessfully() {
  //
  //    // Given
  //    PlanMealAssembler planMealAssembler = mock(PlanMealAssembler.class);
  //
  //    NutritionPlanAssemblerImpl assembler = new NutritionPlanAssemblerImpl(planMealAssembler);
  //
  //    PatientProfile patient = mock(PatientProfile.class);
  //
  //    given(patient.getPrimaryGoal()).willReturn(GoalType.WEIGHT_LOSS);
  //    given(patient.getId()).willReturn(1L);
  //
  //    Calories calories = new Calories(2000);
  //
  //    MacroDistribution macros = new MacroDistribution(150, 250, 67);
  //
  //    // When
  //    NutritionPlan result = assembler.assemble(patient, calories, macros);
  //
  //    // Then
  //    assertNotNull(result);
  //
  //    assertEquals(DayOfWeek.values().length, result.getWeek().size());
  //
  //    verify(planMealAssembler, times(7))
  //        .assemble(
  //            any(DailyPlan.class),
  //            eq(patient),
  //            eq(calories),
  //            eq(macros),
  //            anySet(), // 5to argumento: additionalExcludedTags (Set<FoodTag>)
  //            anySet() // 6to argumento: usedFoodIdsInDay (Set<Long>)
  //            );
  //  }
}
