package com.lirium.nutrition.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaloriesTest {

    @Test
    void shouldCreateCalories() {

        Calories calories = new Calories(2000);

        assertThat(calories.amount()).isEqualTo(2000);
    }

    @Test
    void shouldAcceptMinimumCalories() {

        Calories calories = new Calories(0);

        assertThat(calories.amount()).isZero();
    }

    @Test
    void shouldAcceptMaximumCalories() {

        Calories calories = new Calories(200000);

        assertThat(calories.amount()).isEqualTo(200000);
    }

    @Test
    void shouldRejectNegativeCalories() {

        assertThatThrownBy(() -> new Calories(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectCaloriesGreaterThanMaximum() {

        assertThatThrownBy(() -> new Calories(200001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddCalories() {

        Calories result =
                new Calories(1000)
                        .add(new Calories(500));

        assertThat(result.amount()).isEqualTo(1500);
    }

    @Test
    void shouldAddZeroCalories() {

        Calories result =
                new Calories(1000)
                        .add(new Calories(0));

        assertThat(result.amount()).isEqualTo(1000);
    }

    @Test
    void shouldRejectNullCaloriesWhenAdding() {

        Calories calories = new Calories(1000);

        assertThatThrownBy(() -> calories.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectAdditionThatExceedsMaximumCalories() {

        Calories calories = new Calories(200000);

        assertThatThrownBy(() ->
                calories.add(new Calories(1))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnTrueWhenCaloriesAreZero() {

        assertThat(new Calories(0).isZero())
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenCaloriesAreNotZero() {

        assertThat(new Calories(100).isZero())
                .isFalse();
    }

    @Test
    void shouldReturnDisplayString() {

        assertThat(new Calories(1500).toDisplayString())
                .isEqualTo("1500 kcal");
    }
}