package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanMealAssemblerImplTest {

    @Test
    void shouldDelegateToMainAssembleMethod() {

        // Given
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PatientProfile patient = mock(PatientProfile.class);
        Calories calories = mock(Calories.class);
        MacroDistribution macros = mock(MacroDistribution.class);

        PlanFoodPortionAssembler assembler = mock(PlanFoodPortionAssembler.class);

        PlanMealAssemblerImpl service = new PlanMealAssemblerImpl(assembler);

        when(calories.amount()).thenReturn(1000);
        when(macros.fatGrams()).thenReturn(100);
        when(macros.carbGrams()).thenReturn(200);
        when(macros.proteinGrams()).thenReturn(150);

        // When
        service.assemble(dailyPlan, patient, calories, macros);

        // Then
        verify(assembler, times(MealType.values().length))
                .assemble(any(), eq(patient), eq(Collections.emptySet()),
                        any(), any(), any(), any());

        verify(dailyPlan, times(MealType.values().length))
                .addMeal(any());
    }

    @Test
    void shouldAssembleMealsForAllMealTypes() {

        // Given
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PatientProfile patient = mock(PatientProfile.class);
        Calories calories = mock(Calories.class);
        MacroDistribution macros = mock(MacroDistribution.class);

        PlanFoodPortionAssembler assembler = mock(PlanFoodPortionAssembler.class);

        PlanMealAssemblerImpl service = new PlanMealAssemblerImpl(assembler);

        when(calories.amount()).thenReturn(1000);

        when(macros.fatGrams()).thenReturn(100);
        when(macros.carbGrams()).thenReturn(200);
        when(macros.proteinGrams()).thenReturn(150);

        // When
        service.assemble(dailyPlan, patient, calories, macros, Collections.emptySet());

        // Then
        verify(assembler, times(MealType.values().length))
                .assemble(any(), eq(patient), eq(Collections.emptySet()), any(), any(), any(), any());

        verify(dailyPlan, times(MealType.values().length))
                .addMeal(any());
    }

}