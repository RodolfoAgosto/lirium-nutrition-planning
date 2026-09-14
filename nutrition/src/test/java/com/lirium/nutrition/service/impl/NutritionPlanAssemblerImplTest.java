package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.PlanMealAssembler;
import java.time.DayOfWeek;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionPlanAssemblerImplTest {

  @Test
  void shouldAssembleNutritionPlanSuccessfully() {

    // Given
    PlanMealAssembler planMealAssembler = mock(PlanMealAssembler.class);

    NutritionPlanAssemblerImpl assembler = new NutritionPlanAssemblerImpl(planMealAssembler);

    PatientProfile patient = mock(PatientProfile.class);

    given(patient.getPrimaryGoal()).willReturn(GoalType.WEIGHT_LOSS);
    given(patient.getId()).willReturn(1L);

    Calories calories = new Calories(2000);

    MacroDistribution macros = new MacroDistribution(150, 250, 67);

    // When
    NutritionPlan result = assembler.assemble(patient, calories, macros);

    // Then
    assertNotNull(result);

    assertEquals(DayOfWeek.values().length, result.getWeek().size());

    verify(planMealAssembler, times(7))
        .assemble(
            any(DailyPlan.class),
            eq(patient),
            eq(calories),
            eq(macros),
            anySet(), // 5to argumento: additionalExcludedTags (Set<FoodTag>)
            anySet() // 6to argumento: usedFoodIdsInDay (Set<Long>)
            );
  }
}
