package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PlanFoodPortionTest {

    private Food food;
    private Food anotherFood;
    private PlanMeal meal;

    @BeforeEach
    void setUp() {

        food = Food.of(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        anotherFood = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.SNACK)
        );

        meal = mock(PlanMeal.class);
    }


    @Test
    void shouldCreatePlanFoodPortion() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );


        assertThat(portion.getFood())
                .isEqualTo(food);

        assertThat(portion.getQuantity())
                .isEqualTo(100.0);

        assertThat(portion.getUnit())
                .isEqualTo(MeasureUnit.GRAM);

        assertThat(portion.getMeal())
                .isEqualTo(meal);
    }


    @Test
    void shouldNotCreateWithoutFood() {

        assertThatThrownBy(() ->
                PlanFoodPortion.of(
                        meal,
                        null,
                        100.0,
                        MeasureUnit.GRAM
                )
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldNotCreateWithoutQuantity() {

        assertThatThrownBy(() ->
                PlanFoodPortion.of(
                        meal,
                        food,
                        null,
                        MeasureUnit.GRAM
                )
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldChangeQuantity() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );


        portion.changeQuantity(250.0);


        assertThat(portion.getQuantity())
                .isEqualTo(250.0);
    }


    @Test
    void shouldChangeFood() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );


        portion.changeFood(anotherFood);


        assertThat(portion.getFood())
                .isEqualTo(anotherFood);
    }


    @Test
    void shouldAssignToAnotherMeal() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );

        PlanMeal anotherMeal = mock(PlanMeal.class);


        portion.assignToMeal(anotherMeal);


        assertThat(portion.getMeal())
                .isEqualTo(anotherMeal);
    }


    @Test
    void shouldNotChangeFoodToNull() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );


        assertThatThrownBy(() ->
                portion.changeFood(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNegativeQuantity() {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );

        assertThatThrownBy(() ->
                portion.changeQuantity(-10.0)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

}