package com.lirium.nutrition.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanMealAssemblerImplTest {

  @Mock private PlanFoodPortionAssembler planFoodPortionAssembler;

  @InjectMocks private PlanMealAssemblerImpl planMealAssembler;

  private DailyPlan dailyPlan;
  private PatientProfile patientProfile;
  private Calories calories;
  private MacroDistribution macros;
  private NutrientBudget dummyConsumed;
  private Set<Long> usedFoodIdsInDay;

  @BeforeEach
  void setUp() {
    dailyPlan = mock(DailyPlan.class);
    patientProfile = mock(PatientProfile.class);

    when(patientProfile.getRestrictions()).thenReturn(Collections.emptySet());

    calories = new Calories(2000);
    macros = new MacroDistribution(200, 70, 150);
    usedFoodIdsInDay = new HashSet<>();

    dummyConsumed =
        new NutrientBudget(new Calories(100), new Carbs(10), new Fat(5), new Protein(15));
  }

  //  @Test
  //  @DisplayName("Debería ensamblar una comida por cada MealType del enum")
  //  void shouldAssembleAllMealsInDay() {
  //    // Given
  //    when(planFoodPortionAssembler.assemble(any(), any(), any(),
  // any())).thenReturn(dummyConsumed);
  //
  //    // When
  //    planMealAssembler.assemble(dailyPlan, patientProfile, calories, macros, usedFoodIdsInDay);
  //
  //    // Then
  //    int totalMeals = MealType.values().length;
  //    verify(planFoodPortionAssembler, times(totalMeals))
  //        .assemble(any(), any(), eq(Collections.emptySet()), eq(usedFoodIdsInDay));
  //    verify(dailyPlan, times(totalMeals)).addMeal(any());
  //  }
  //
  //  @Test
  //  @DisplayName("Debería combinar restricciones del paciente con tags adicionales excluidos")
  //  void shouldMergePatientRestrictionsAndAdditionalExcludedTags() {
  //    // Given
  //    Restriction restriction = mock(Restriction.class);
  //    when(restriction.getExcludedTags()).thenReturn(Set.of(FoodTag.GLUTEN));
  //    when(patientProfile.getRestrictions()).thenReturn(Set.of(restriction));
  //
  //    Set<FoodTag> additionalTags = Set.of(FoodTag.HONEY);
  //
  //    when(planFoodPortionAssembler.assemble(any(), any(), any(),
  // any())).thenReturn(dummyConsumed);
  //
  //    // When
  //    planMealAssembler.assemble(
  //        dailyPlan, patientProfile, calories, macros, additionalTags, usedFoodIdsInDay);
  //
  //    // Then
  //    @SuppressWarnings("unchecked")
  //    ArgumentCaptor<Set<FoodTag>> captor = ArgumentCaptor.forClass(Set.class);
  //
  //    verify(planFoodPortionAssembler, times(MealType.values().length))
  //        .assemble(any(), any(), captor.capture(), eq(usedFoodIdsInDay));
  //
  //    Set<FoodTag> capturedTags = captor.getValue();
  //    assertTrue(capturedTags.contains(FoodTag.GLUTEN));
  //    assertTrue(capturedTags.contains(FoodTag.HONEY));
  //  }

  //  @Test
  //  @DisplayName("Debería pasar el Set de alimentos usados a través de todas las comidas del día")
  //  void shouldPassUsedFoodIdsAcrossAllMealsInDay() {
  //    // Given
  //    usedFoodIdsInDay.add(10L);
  //    when(planFoodPortionAssembler.assemble(any(), any(), any(),
  // any())).thenReturn(dummyConsumed);
  //
  //    // When
  //    assertDoesNotThrow(
  //        () ->
  //            planMealAssembler.assemble(
  //                dailyPlan, patientProfile, calories, macros, usedFoodIdsInDay));
  //
  //    // Then
  //    verify(planFoodPortionAssembler, times(MealType.values().length))
  //        .assemble(any(), any(), any(), eq(usedFoodIdsInDay));
  //  }
}
