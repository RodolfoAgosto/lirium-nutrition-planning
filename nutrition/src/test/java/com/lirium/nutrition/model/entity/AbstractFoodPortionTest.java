package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFoodPortionTest {

    private static class TestFoodPortion extends AbstractFoodPortion {

        TestFoodPortion(Food food, Double quantity, MeasureUnit unit) {
            super(food, quantity, unit);
        }
    }

    private Food chicken;

    @BeforeEach
    void setUp() {

        chicken = Food.of(
                "Chicken",
                200,
                30,
                10,
                20,
                FoodCategory.PROTEIN,
                EnumSet.allOf(MealType.class)
        );
    }

    @Test
    @DisplayName("Debe crear una porción válida")
    void shouldCreateValidPortion() {

        TestFoodPortion portion = new TestFoodPortion(chicken, 100.0, MeasureUnit.GRAM);

        assertNotNull(portion);
        assertEquals(100.0, portion.grams());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el alimento es nulo")
    void shouldThrowWhenFoodIsNull() {

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new TestFoodPortion(null, 100.0, MeasureUnit.GRAM)
        );

        assertEquals("Food cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cantidad es nula")
    void shouldThrowWhenQuantityIsNull() {

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new TestFoodPortion(chicken, null, MeasureUnit.GRAM)
        );

        assertEquals("Quantity cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la unidad es nula")
    void shouldThrowWhenUnitIsNull() {

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new TestFoodPortion(chicken, 100.0, null)
        );

        assertEquals("Unit cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cantidad es cero")
    void shouldThrowWhenQuantityIsZero() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestFoodPortion(chicken, 0.0, MeasureUnit.GRAM)
        );

        assertEquals("Quantity must be positive", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cantidad es negativa")
    void shouldThrowWhenQuantityIsNegative() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestFoodPortion(chicken, -10.0, MeasureUnit.GRAM)
        );

        assertEquals("Quantity must be positive", ex.getMessage());
    }



}