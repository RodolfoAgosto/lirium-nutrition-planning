package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class NutrientBudgetTest {

  private static NutrientBudget budget(int calories, int protein, int carbs, int fat) {
    return new NutrientBudget(
        new Calories(calories), new Carbs(carbs), new Fat(fat), new Protein(protein));
  }

  @Test
  void shouldAddEachNutrient() {
    NutrientBudget sum = budget(100, 10, 20, 5).add(budget(50, 5, 10, 2));

    assertEquals(budget(150, 15, 30, 7), sum);
  }

  @Test
  void shouldAverageAndRoundEachNutrient() {
    NutrientBudget average =
        NutrientBudget.average(List.of(budget(2000, 100, 300, 60), budget(2001, 101, 301, 61)))
            .orElseThrow();

    // 2000.5 rounds up; same for each macro
    assertEquals(budget(2001, 101, 301, 61), average);
  }

  @Test
  void shouldHaveNoAverageWithoutData() {
    assertTrue(NutrientBudget.average(List.of()).isEmpty());
  }
}
