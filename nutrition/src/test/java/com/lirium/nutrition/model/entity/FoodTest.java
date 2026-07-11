package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FoodTest {


    private Food createFood() {
        return Food.of(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );
    }


    @Test
    void shouldCreateFoodWithValidData() {

        Food food = createFood();

        assertEquals("Rice", food.getName());
        assertEquals(130, food.getCaloriesPer100g());
        assertEquals(FoodCategory.CARB, food.getCategory());
        assertEquals(MeasureUnit.GRAM, food.getDefaultUnit());
        assertTrue(food.getSuitableFor().contains(MealType.LUNCH));
    }


    @Test
    void shouldRejectNullName() {

        assertThrows(
                NullPointerException.class,
                () -> Food.of(
                        null,
                        100,
                        10,
                        10,
                        5,
                        FoodCategory.CARB,
                        null
                )
        );
    }


    @Test
    void shouldRejectBlankName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Food.of(
                        " ",
                        100,
                        10,
                        10,
                        5,
                        FoodCategory.CARB,
                        null
                )
        );
    }


    @Test
    void shouldRejectInvalidCaloriesRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Food.of(
                        "Apple",
                        2000,
                        1,
                        20,
                        0,
                        FoodCategory.FRUIT,
                        null
                )
        );
    }


    @Test
    void shouldConvertGramsCorrectly() {

        Food food = createFood();

        Double grams = food.toGrams(
                200D,
                MeasureUnit.GRAM
        );

        assertEquals(200D, grams);
    }


    @Test
    void shouldConvertMillilitersUsingDensity() {

        Food milk = Food.ofLiquid(
                "Milk",
                60,
                3,
                5,
                3,
                FoodCategory.DAIRY,
                null,
                1.03
        );

        assertEquals(
                103D,
                milk.toGrams(100D, MeasureUnit.MILLILITER)
        );
    }


    @Test
    void shouldFailConvertMillilitersWithoutDensity() {

        Food food = createFood();

        assertThrows(
                IllegalStateException.class,
                () -> food.toGrams(100D, MeasureUnit.MILLILITER)
        );
    }


    @Test
    void shouldConvertUnitsUsingWeight() {

        Food egg = Food.ofUnit(
                "Egg",
                150,
                12,
                1,
                10,
                FoodCategory.PROTEIN,
                null,
                50D
        );

        assertEquals(
                100D,
                egg.toGrams(2D, MeasureUnit.UNIT)
        );
    }


    @Test
    void shouldFailConvertUnitsWithoutWeight() {

        Food food = createFood();

        assertThrows(
                IllegalStateException.class,
                () -> food.toGrams(2D, MeasureUnit.UNIT)
        );
    }


    @Test
    void shouldAddAndRemoveSuitableFor() {

        Food food = createFood();

        food.addSuitableFor(MealType.BREAKFAST);

        assertTrue(
                food.getSuitableFor()
                        .contains(MealType.BREAKFAST)
        );

        food.removeSuitableFor(MealType.BREAKFAST);

        assertFalse(
                food.getSuitableFor()
                        .contains(MealType.BREAKFAST)
        );
    }


    @Test
    void shouldNotAllowNullMealType() {

        Food food = createFood();

        assertThrows(
                NullPointerException.class,
                () -> food.addSuitableFor(null)
        );
    }


    @Test
    void shouldChangeName() {

        Food food = createFood();

        food.changeName("Brown Rice");

        assertEquals(
                "Brown Rice",
                food.getName()
        );
    }


    @Test
    void shouldChangeNutrients() {

        Food food = createFood();

        food.changeCalories(200);
        food.changeProtein(5);
        food.changeCarbs(40);
        food.changeFat(2);

        assertEquals(200, food.getCaloriesPer100g());
        assertEquals(5, food.getProteinPer100g());
        assertEquals(40, food.getCarbsPer100g());
        assertEquals(2, food.getFatPer100g());
    }


    @Test
    void shouldManageTags() {

        Food food = createFood();

        food.addTag(FoodTag.GELATIN);

        assertTrue(
                food.getFoodTags()
                        .contains(FoodTag.GELATIN)
        );

        food.removeTag(FoodTag.GELATIN);

        assertFalse(
                food.getFoodTags()
                        .contains(FoodTag.GELATIN)
        );
    }


    @Test
    void shouldReplaceTags() {

        Food food = createFood();

        food.addTag(FoodTag.GELATIN);

        food.replaceTags(
                Set.of(FoodTag.LACTOSE)
        );

        assertFalse(food.getFoodTags().contains(FoodTag.GELATIN));
        assertTrue(food.getFoodTags().contains(FoodTag.LACTOSE));
    }


    @Test
    void shouldClearTags() {

        Food food = createFood();

        food.addTag(FoodTag.GELATIN);

        food.clearTags();

        assertTrue(
                food.getFoodTags().isEmpty()
        );
    }

}