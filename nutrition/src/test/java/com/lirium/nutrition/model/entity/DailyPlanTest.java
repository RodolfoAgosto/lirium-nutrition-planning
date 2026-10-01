package com.lirium.nutrition.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.exception.PlanConflictException;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.valueobject.NutrientBudget;
import java.time.DayOfWeek;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyPlanTest {

  private NutritionPlan nutritionPlan;
  private DailyPlan dailyPlan;

  @BeforeEach
  void setUp() {

    nutritionPlan = NutritionPlan.generate(GoalType.WEIGHT_LOSS, 2000, 150, 200, 60, null);

    dailyPlan = DailyPlan.of(DayOfWeek.MONDAY, nutritionPlan);
  }

  @Test
  @DisplayName("Debe crear un DailyPlan correctamente")
  void shouldCreateDailyPlan() {

    assertNotNull(dailyPlan);
    assertEquals(DayOfWeek.MONDAY, dailyPlan.getDayOfWeek());
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el día es null")
  void shouldThrowWhenDayIsNull() {

    assertThrows(NullPointerException.class, () -> DailyPlan.of(null, nutritionPlan));
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el nutrition plan es null")
  void shouldThrowWhenNutritionPlanIsNull() {

    assertThrows(NullPointerException.class, () -> DailyPlan.of(DayOfWeek.MONDAY, null));
  }

  @Test
  @DisplayName("Debe agregar una comida al día")
  void shouldAddMeal() {

    PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

    dailyPlan.addMeal(meal);

    assertEquals(1, dailyPlan.getMeals().size());
    assertTrue(dailyPlan.getMeals().contains(meal));
  }

  @Test
  @DisplayName("Debe rechazar agregar una comida null")
  void shouldThrowWhenAddingNullMeal() {

    assertThrows(NullPointerException.class, () -> dailyPlan.addMeal(null));
  }

  @Test
  @DisplayName("No debe permitir dos comidas del mismo tipo")
  void shouldNotAllowDuplicateMealType() {

    PlanMeal breakfast1 = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

    PlanMeal breakfast2 = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

    dailyPlan.addMeal(breakfast1);

    assertThrows(PlanConflictException.class, () -> dailyPlan.addMeal(breakfast2));
  }

  @Test
  @DisplayName("Debe eliminar una comida")
  void shouldRemoveMeal() {

    PlanMeal meal = PlanMeal.of(MealType.LUNCH, dailyPlan);

    dailyPlan.addMeal(meal);

    dailyPlan.removeMeal(meal);

    assertTrue(dailyPlan.getMeals().isEmpty());
  }

  @Test
  @DisplayName("Debe lanzar excepción al eliminar una comida null")
  void shouldThrowWhenRemovingNullMeal() {

    assertThrows(NullPointerException.class, () -> dailyPlan.removeMeal(null));
  }

  @Test
  @DisplayName("Debe limpiar todas las comidas")
  void shouldClearMeals() {

    dailyPlan.addMeal(PlanMeal.of(MealType.BREAKFAST, dailyPlan));

    dailyPlan.addMeal(PlanMeal.of(MealType.DINNER, dailyPlan));

    dailyPlan.clearMeals();

    assertTrue(dailyPlan.getMeals().isEmpty());
  }

  @Test
  @DisplayName("Debe devolver lista de comidas no modificable")
  void shouldReturnUnmodifiableMeals() {

    assertThrows(
        UnsupportedOperationException.class,
        () -> dailyPlan.getMeals().add(PlanMeal.of(MealType.BREAKFAST, dailyPlan)));
  }

  @Test
  @DisplayName("Debe sumar los nutrientes planificados de todas las comidas del día")
  void shouldSumPlannedNutrientsAcrossMeals() {

    Food rice = Food.of("Rice", 130, 3, 28, 1, FoodCategory.CARB, Set.of(MealType.LUNCH));
    Food chicken =
        Food.of("Chicken", 200, 30, 10, 20, FoodCategory.PROTEIN, Set.of(MealType.DINNER));

    PlanMeal lunch = PlanMeal.of(MealType.LUNCH, dailyPlan);
    lunch.addFoodPortion(PlanFoodPortion.of(lunch, rice, 200.0, MeasureUnit.GRAM));
    dailyPlan.addMeal(lunch);

    PlanMeal dinner = PlanMeal.of(MealType.DINNER, dailyPlan);
    dinner.addFoodPortion(PlanFoodPortion.of(dinner, chicken, 150.0, MeasureUnit.GRAM));
    dailyPlan.addMeal(dinner);

    NutrientBudget planned = dailyPlan.plannedNutrients();

    // rice 200 g = 260 kcal · 6 P · 56 C · 2 F | chicken 150 g = 300 kcal · 45 P · 15 C · 30 F
    assertEquals(560, planned.calories().amount());
    assertEquals(51, planned.protein().grams());
    assertEquals(71, planned.carbs().amount());
    assertEquals(32, planned.fat().amount());
  }

  @Test
  @DisplayName("Un día sin comidas no tiene nada planificado")
  void shouldReturnZeroWhenDayHasNoMeals() {

    assertEquals(NutrientBudget.ZERO, dailyPlan.plannedNutrients());
  }
}
