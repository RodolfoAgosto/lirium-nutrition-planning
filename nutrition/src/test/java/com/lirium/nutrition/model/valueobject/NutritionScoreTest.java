package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class NutritionScoreTest {

  private static NutrientBudget budget(int calories, int protein, int carbs, int fat) {
    return new NutrientBudget(
        new Calories(calories), new Carbs(carbs), new Fat(fat), new Protein(protein));
  }

  @Test
  void shouldScore100WhenIntakeMatchesThePlan() {
    NutritionScore score =
        NutritionScore.of(budget(2000, 150, 200, 70), budget(2000, 150, 200, 70)).orElseThrow();

    assertEquals(100.0, score.overall());
  }

  @Test
  void shouldWeightCaloriesFortyAndEachMacroTwenty() {
    // protein at half the plan -> protein 50, overall = 0.4*100 + 0.2*(50+100+100) = 90
    NutritionScore score =
        NutritionScore.of(budget(2000, 150, 200, 70), budget(2000, 75, 200, 70)).orElseThrow();

    assertAll(
        () -> assertEquals(100.0, score.calories()),
        () -> assertEquals(50.0, score.protein()),
        () -> assertEquals(90.0, score.overall()));
  }

  @Test
  void shouldPenalizeOverAndUnderEatingEqually() {
    double under =
        NutritionScore.of(budget(100, 0, 0, 0), budget(80, 0, 0, 0)).orElseThrow().calories();
    double over =
        NutritionScore.of(budget(100, 0, 0, 0), budget(120, 0, 0, 0)).orElseThrow().calories();

    assertEquals(80.0, under);
    assertEquals(80.0, over);
  }

  @Test
  void shouldBeEmptyWhenNothingWasPlanned() {
    assertTrue(NutritionScore.of(NutrientBudget.ZERO, budget(500, 20, 50, 10)).isEmpty());
  }

  @Test
  void shouldScore100ForAMacroPlannedAtZeroAndNotEaten() {
    NutritionScore score =
        NutritionScore.of(budget(1000, 50, 100, 0), budget(1000, 50, 100, 0)).orElseThrow();

    assertEquals(100.0, score.fat());
  }

  @Test
  void shouldAverageDailyScores() {
    NutritionScore perfect =
        NutritionScore.of(budget(2000, 150, 200, 70), budget(2000, 150, 200, 70)).orElseThrow();
    NutritionScore halfProtein =
        NutritionScore.of(budget(2000, 150, 200, 70), budget(2000, 75, 200, 70)).orElseThrow();

    NutritionScore average = NutritionScore.average(List.of(perfect, halfProtein)).orElseThrow();

    assertAll(
        () -> assertEquals(75.0, average.protein()), () -> assertEquals(95.0, average.overall()));
  }

  @Test
  void shouldHaveNoAverageWithoutScores() {
    assertTrue(NutritionScore.average(List.of()).isEmpty());
  }
}
