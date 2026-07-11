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


class PlanMealTest {

    private DailyPlan dailyPlan;
    private Food rice;
    private Food apple;

    @BeforeEach
    void setUp() {

        dailyPlan = mock(DailyPlan.class);

        rice = Food.of(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        apple = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.SNACK)
        );
    }


    @Test
    void shouldCreatePlanMeal() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);


        assertThat(meal.getType())
                .isEqualTo(MealType.LUNCH);

        assertThat(meal.getFoodPortions())
                .isEmpty();
    }


    @Test
    void shouldNotCreateWithoutMealType() {

        assertThatThrownBy(() ->
                PlanMeal.of(null, dailyPlan)
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldNotCreateWithoutDailyPlan() {

        assertThatThrownBy(() ->
                PlanMeal.of(MealType.LUNCH, null)
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldAddFoodPortion() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        rice,
                        100.0,
                        MeasureUnit.GRAM
                );


        meal.addFoodPortion(portion);


        assertThat(meal.getFoodPortions())
                .containsExactly(portion);

        assertThat(portion.getMeal())
                .isEqualTo(meal);
    }


    @Test
    void shouldNotAddDuplicateFood() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);

        PlanFoodPortion first =
                PlanFoodPortion.of(
                        meal,
                        rice,
                        100.0,
                        MeasureUnit.GRAM
                );

        PlanFoodPortion second =
                PlanFoodPortion.of(
                        meal,
                        rice,
                        200.0,
                        MeasureUnit.GRAM
                );


        meal.addFoodPortion(first);
        meal.addFoodPortion(second);


        assertThat(meal.getFoodPortions())
                .hasSize(1);
    }


    @Test
    void shouldRemoveFoodPortion() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        rice,
                        100.0,
                        MeasureUnit.GRAM
                );

        meal.addFoodPortion(portion);


        meal.removeFoodPortion(portion);


        assertThat(meal.getFoodPortions())
                .isEmpty();
    }


    @Test
    void shouldNotExposeInternalFoodList() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);


        assertThatThrownBy(() ->
                meal.getFoodPortions().clear()
        )
                .isInstanceOf(UnsupportedOperationException.class);
    }


    @Test
    void shouldNotAddNullFoodPortion() {

        PlanMeal meal =
                PlanMeal.of(MealType.LUNCH, dailyPlan);


        assertThatThrownBy(() ->
                meal.addFoodPortion(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

}