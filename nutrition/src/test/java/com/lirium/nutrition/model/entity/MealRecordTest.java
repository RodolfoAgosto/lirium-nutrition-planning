package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MealRecordTest {

    @Test
    void shouldCreateManualMealRecord() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        assertEquals(MealType.LUNCH, meal.getType());
        assertFalse(meal.isOverridden());
        assertTrue(meal.getFoodPortions().isEmpty());
    }


    @Test
    void shouldRejectNullMealType() {

        assertThrows(
                NullPointerException.class,
                () -> MealRecord.of(
                        null,
                        LocalDateTime.now(),
                        createDailyRecord()
                )
        );
    }


    @Test
    void shouldRejectFutureDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> MealRecord.of(
                        MealType.DINNER,
                        LocalDateTime.now().plusDays(1),
                        createDailyRecord()
                )
        );
    }


    @Test
    void shouldAddFoodPortion() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        Food food = createFood();


        meal.addFoodPortion(
                food,
                100D,
                MeasureUnit.GRAM
        );


        assertEquals(
                1,
                meal.getFoodPortions().size()
        );

        assertEquals(
                food,
                meal.getFoodPortions()
                        .get(0)
                        .getFood()
        );
    }


    @Test
    void shouldNotAddDuplicateFoodPortion() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        Food food = createFood();


        meal.addFoodPortion(
                food,
                100D,
                MeasureUnit.GRAM
        );

        meal.addFoodPortion(
                food,
                100D,
                MeasureUnit.GRAM
        );


        assertEquals(
                1,
                meal.getFoodPortions().size()
        );
    }


    @Test
    void shouldRemoveFoodPortion() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        Food food = createFood();

        meal.addFoodPortion(
                food,
                100D,
                MeasureUnit.GRAM
        );


        FoodPortionRecord portion =
                meal.getFoodPortions().get(0);


        meal.removeFoodPortion(portion);


        assertTrue(
                meal.getFoodPortions().isEmpty()
        );
    }


    @Test
    void shouldClearFoods() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        meal.addFoodPortion(
                createFood(),
                100D,
                MeasureUnit.GRAM
        );


        meal.clearFoods();


        assertTrue(
                meal.getFoodPortions().isEmpty()
        );
    }


    @Test
    void shouldMarkAsOverridden() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        meal.markAsOverridden();


        assertTrue(
                meal.isOverridden()
        );
    }


    @Test
    void shouldMarkAsOverriddenWithReason() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        meal.markAsOverridden("Changed because patient was sick");


        assertTrue(meal.isOverridden());
        assertEquals(
                "Changed because patient was sick",
                meal.getNotes()
        );
    }


    @Test
    void shouldRejectBlankOverrideReason() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        assertThrows(
                IllegalArgumentException.class,
                () -> meal.markAsOverridden(" ")
        );
    }


    @Test
    void shouldUpdateNotes() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        meal.updateNotes("Ate outside plan");


        assertEquals(
                "Ate outside plan",
                meal.getNotes()
        );
    }


    @Test
    void shouldClearOverride() {

        MealRecord meal =
                MealRecord.of(
                        MealType.LUNCH,
                        LocalDateTime.now(),
                        createDailyRecord()
                );


        meal.markAsOverridden("Reason");

        meal.clearOverride();


        assertFalse(meal.isOverridden());
        assertNull(meal.getNotes());
    }

    private DailyRecord createDailyRecord() {

        User user = new User(
                "test@test.com",
                "password",
                "Test",
                "User",
                Role.PATIENT
        );

        PatientProfile patient = user.getPatientProfile();

        return DailyRecord.of(
                patient,
                java.time.LocalDate.now()
        );
    }

    private Food createFood() {

        return Food.of(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                null
        );
    }

}