package com.lirium.nutrition.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class MealRecordTest {

  @Test
  void shouldCreateManualMealRecord() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    assertEquals(MealType.LUNCH, meal.getType());
    assertFalse(meal.isOverridden());
    assertTrue(meal.getFoodPortions().isEmpty());
  }

  @Test
  void shouldRejectNullMealType() {
    assertThrows(
            NullPointerException.class,
            () -> MealRecord.of(null, todayAtNoon(), createDailyRecord()));
  }

  @Test
  void shouldRejectFutureDate() {
    LocalDateTime tomorrow =
            LocalDate.now(ARGENTINA_ZONE).plusDays(1).atTime(12, 0);

    assertThrows(
            IllegalArgumentException.class,
            () -> MealRecord.of(MealType.DINNER, tomorrow, createDailyRecord()));
  }

  @Test
  void shouldAddFoodPortion() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    Food food = createFood();

    meal.addFoodPortion(food, 100D, MeasureUnit.GRAM);

    assertEquals(1, meal.getFoodPortions().size());
    assertEquals(food, meal.getFoodPortions().get(0).getFood());
  }

  @Test
  void shouldNotAddDuplicateFoodPortion() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    Food food = createFood();

    meal.addFoodPortion(food, 100D, MeasureUnit.GRAM);
    meal.addFoodPortion(food, 100D, MeasureUnit.GRAM);

    assertEquals(1, meal.getFoodPortions().size());
  }

  @Test
  void shouldRemoveFoodPortion() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    Food food = createFood();

    meal.addFoodPortion(food, 100D, MeasureUnit.GRAM);

    FoodPortionRecord portion = meal.getFoodPortions().get(0);

    meal.removeFoodPortion(portion);

    assertTrue(meal.getFoodPortions().isEmpty());
  }

  @Test
  void shouldClearFoods() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    meal.addFoodPortion(createFood(), 100D, MeasureUnit.GRAM);

    meal.clearFoods();

    assertTrue(meal.getFoodPortions().isEmpty());
  }

  @Test
  void shouldMarkAsOverridden() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    meal.markAsOverridden();

    assertTrue(meal.isOverridden());
  }

  @Test
  void shouldMarkAsOverriddenWithReason() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    meal.markAsOverridden("Changed because patient was sick");

    assertTrue(meal.isOverridden());
    assertEquals("Changed because patient was sick", meal.getNotes());
  }

  @Test
  void shouldRejectBlankOverrideReason() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    assertThrows(
            IllegalArgumentException.class,
            () -> meal.markAsOverridden(" "));
  }

  @Test
  void shouldUpdateNotes() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    meal.updateNotes("Ate outside plan");

    assertEquals("Ate outside plan", meal.getNotes());
  }

  @Test
  void shouldClearOverride() {
    MealRecord meal =
            MealRecord.of(MealType.LUNCH, todayAtNoon(), createDailyRecord());

    meal.markAsOverridden("Reason");

    meal.clearOverride();

    assertFalse(meal.isOverridden());
    assertNull(meal.getNotes());
  }

  private LocalDateTime todayAtNoon() {
    return LocalDate.now(ARGENTINA_ZONE).atTime(12, 0);
  }

  private DailyRecord createDailyRecord() {
    User user =
            new User("test@test.com", "password", "Test", "User", Role.PATIENT);

    PatientProfile patient = user.getPatientProfile();

    return DailyRecord.of(
            patient,
            LocalDate.now(ARGENTINA_ZONE));
  }

  private Food createFood() {
    return Food.of("Rice", 130, 3, 28, 1, FoodCategory.CARB, null);
  }

  private static final ZoneId ARGENTINA_ZONE =   ZoneId.of("America/Argentina/Buenos_Aires");

}