package com.lirium.nutrition.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.service.PlanMealAssembler;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionPlanAssemblerImplTest {

  private static final Calories CALORIES = new Calories(2000);
  private static final MacroDistribution MACROS = new MacroDistribution(150, 250, 70);
  private static final int DAYS_IN_WEEK = DayOfWeek.values().length;

  @Mock private PlanMealAssembler planMealAssembler;

  private NutritionPlanAssemblerImpl assembler;
  private PatientProfile patient;

  @BeforeEach
  void setUp() {
    assembler = new NutritionPlanAssemblerImpl(planMealAssembler);
    patient = new PatientProfile(new User());
    patient.updateNutritionProfile(
        Height.of(170), Weight.of(70000), ActivityLevel.MODERATE, GoalType.WEIGHT_LOSS);
  }

  @Test
  @DisplayName("Builds a DRAFT plan with the patient's goal and the given targets")
  void shouldBuildDraftPlanWithGoalAndTargets() {

    NutritionPlan plan = assembler.assemble(patient, CALORIES, MACROS);

    assertThat(plan.getStatus()).isEqualTo(PlanStatus.DRAFT);
    assertThat(plan.getTargetGoal()).isEqualTo(GoalType.WEIGHT_LOSS);
    assertThat(plan.getDailyCalories()).isEqualTo(2000);
    assertThat(plan.getProteinGrams()).isEqualTo(150);
    assertThat(plan.getCarbGrams()).isEqualTo(250);
    assertThat(plan.getFatGrams()).isEqualTo(70);
    assertThat(plan.getPatientProfile()).isSameAs(patient);
  }

  @Test
  @DisplayName("Creates the seven days of the week, in order, and assembles each one")
  void shouldAssembleEveryDayOfTheWeekInOrder() {

    NutritionPlan plan = assembler.assemble(patient, CALORIES, MACROS);

    assertThat(plan.getWeek())
        .extracting(DailyPlan::getDayOfWeek)
        .containsExactly(DayOfWeek.values());
    verify(planMealAssembler, times(DAYS_IN_WEEK))
        .assemble(
            any(), eq(patient), eq(CALORIES), eq(MACROS), anySet(), anySet(), anyMap(), anyMap());
  }

  @Test
  @DisplayName("Weekly frequencies are shared across days; daily usage starts fresh every day")
  @SuppressWarnings("unchecked")
  void shouldShareWeeklyFrequencyButResetDailyUsage() {
    ArgumentCaptor<Set<Long>> usedFoodIdsInDay = ArgumentCaptor.forClass(Set.class);
    ArgumentCaptor<Map<Long, Integer>> foodFrequencyInWeek = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<Map<Long, Double>> foodGramsInDay = ArgumentCaptor.forClass(Map.class);

    assembler.assemble(patient, CALORIES, MACROS);

    verify(planMealAssembler, times(DAYS_IN_WEEK))
        .assemble(
            any(),
            any(),
            any(),
            any(),
            anySet(),
            usedFoodIdsInDay.capture(),
            foodFrequencyInWeek.capture(),
            foodGramsInDay.capture());

    List<Map<Long, Integer>> frequencies = foodFrequencyInWeek.getAllValues();
    assertThat(frequencies).allSatisfy(map -> assertThat(map).isSameAs(frequencies.getFirst()));

    assertThat(identityCount(usedFoodIdsInDay.getAllValues())).isEqualTo(DAYS_IN_WEEK);
    assertThat(identityCount(foodGramsInDay.getAllValues())).isEqualTo(DAYS_IN_WEEK);
  }

  @Test
  @DisplayName("Passes the additional excluded tags to every day")
  void shouldPassAdditionalExcludedTagsToEveryDay() {
    Set<FoodTag> templateExclusions = Set.of(FoodTag.GLUTEN);

    assembler.assemble(patient, CALORIES, MACROS, templateExclusions);

    verify(planMealAssembler, times(DAYS_IN_WEEK))
        .assemble(any(), any(), any(), any(), eq(templateExclusions), anySet(), anyMap(), anyMap());
  }

  @Test
  @DisplayName("Without additional tags, every day receives an empty set")
  void shouldPassEmptyExcludedTagsByDefault() {

    assembler.assemble(patient, CALORIES, MACROS);

    verify(planMealAssembler, times(DAYS_IN_WEEK))
        .assemble(any(), any(), any(), any(), eq(Set.of()), anySet(), anyMap(), anyMap());
  }

  /** How many distinct instances (by identity, not equality) the list contains. */
  private static long identityCount(List<?> values) {
    return values.stream().map(System::identityHashCode).distinct().count();
  }
}
