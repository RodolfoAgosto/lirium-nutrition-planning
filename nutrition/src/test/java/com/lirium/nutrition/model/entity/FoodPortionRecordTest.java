package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MeasureUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodPortionRecordTest {


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


    @Test
    void shouldCreateFoodPortionRecord() {

        MealRecord meal = new MealRecord();

        Food food = createFood();

        FoodPortionRecord portion =
                FoodPortionRecord.of(
                        meal,
                        food,
                        200D,
                        MeasureUnit.GRAM
                );


        assertSame(meal, portion.getMeal());
        assertSame(food, portion.getFood());
        assertEquals(200D, portion.getQuantity());
        assertEquals(MeasureUnit.GRAM, portion.getUnit());
    }


    @Test
    void shouldRejectNullMeal() {

        Food food = createFood();

        assertThrows(
                NullPointerException.class,
                () -> FoodPortionRecord.of(
                        null,
                        food,
                        100D,
                        MeasureUnit.GRAM
                )
        );
    }


    @Test
    void shouldRejectNullFood() {

        MealRecord meal = new MealRecord();

        assertThrows(
                NullPointerException.class,
                () -> FoodPortionRecord.of(
                        meal,
                        null,
                        100D,
                        MeasureUnit.GRAM
                )
        );
    }


    @Test
    void shouldRejectNullQuantity() {

        MealRecord meal = new MealRecord();

        assertThrows(
                NullPointerException.class,
                () -> FoodPortionRecord.of(
                        meal,
                        createFood(),
                        null,
                        MeasureUnit.GRAM
                )
        );
    }


    @Test
    void shouldRejectNegativeQuantity() {

        MealRecord meal = new MealRecord();

        assertThrows(
                IllegalArgumentException.class,
                () -> FoodPortionRecord.of(
                        meal,
                        createFood(),
                        -10D,
                        MeasureUnit.GRAM
                )
        );
    }


    @Test
    void shouldCalculateGrams() {

        MealRecord meal = new MealRecord();

        FoodPortionRecord portion =
                FoodPortionRecord.of(
                        meal,
                        createFood(),
                        150D,
                        MeasureUnit.GRAM
                );


        assertEquals(
                150D,
                portion.grams()
        );
    }


    @Test
    void shouldCalculateCalories() {

        MealRecord meal = new MealRecord();

        FoodPortionRecord portion =
                FoodPortionRecord.of(
                        meal,
                        createFood(),
                        200D,
                        MeasureUnit.GRAM
                );


        // 130 kcal cada 100g -> 260 kcal en 200g
        assertEquals(
                260,
                portion.calories().amount()
        );
    }


    @Test
    void shouldCalculateMacros() {

        MealRecord meal = new MealRecord();

        FoodPortionRecord portion =
                FoodPortionRecord.of(
                        meal,
                        createFood(),
                        200D,
                        MeasureUnit.GRAM
                );


        assertEquals(6, portion.protein().grams());
        assertEquals(56, portion.carbs().amount());
        assertEquals(2, portion.fat().amount());
    }

}