package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.PlanMealAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionPlanAssemblerImplTest {

    @Test
    void shouldAssembleNutritionPlanSuccessfully() {

        // Given
        PlanMealAssembler planMealAssembler = mock(PlanMealAssembler.class);

        NutritionPlanAssemblerImpl assembler =
                new NutritionPlanAssemblerImpl(planMealAssembler);

        PatientProfile patient = mock(PatientProfile.class);

        given(patient.getPrimaryGoal()).willReturn(GoalType.WEIGHT_LOSS);
        given(patient.getId()).willReturn(1L);

        Calories calories = new Calories(2000);

        MacroDistribution macros =
                new MacroDistribution(150, 250, 67);

        // When
        NutritionPlan result =
                assembler.assemble(patient, calories, macros);

        // Then
        assertNotNull(result);

        assertEquals(
                DayOfWeek.values().length,
                result.getWeek().size()
        );

        verify(planMealAssembler, times(7))
                .assemble(
                        any(DailyPlan.class),
                        eq(patient),
                        eq(calories),
                        eq(macros),
                        anySet()
                );

    }


}