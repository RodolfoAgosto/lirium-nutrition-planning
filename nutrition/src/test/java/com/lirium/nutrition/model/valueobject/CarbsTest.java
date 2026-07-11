package com.lirium.nutrition.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarbsTest {

    @Test
    void shouldCreateCarbs() {

        Carbs carbs = new Carbs(100);

        assertThat(carbs.amount()).isEqualTo(100);
    }

    @Test
    void shouldAcceptMinimumCarbs() {

        Carbs carbs = new Carbs(0);

        assertThat(carbs.amount()).isZero();
    }

    @Test
    void shouldAcceptMaximumCarbs() {

        Carbs carbs = new Carbs(1000);

        assertThat(carbs.amount()).isEqualTo(1000);
    }

    @Test
    void shouldRejectNegativeCarbs() {

        assertThatThrownBy(() -> new Carbs(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectCarbsGreaterThanMaximum() {

        assertThatThrownBy(() -> new Carbs(1001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddCarbs() {

        Carbs result =
                new Carbs(100)
                        .add(new Carbs(50));

        assertThat(result.amount()).isEqualTo(150);
    }

    @Test
    void shouldAddZeroCarbs() {

        Carbs result =
                new Carbs(100)
                        .add(new Carbs(0));

        assertThat(result.amount()).isEqualTo(100);
    }

    @Test
    void shouldRejectNullCarbsWhenAdding() {

        Carbs carbs = new Carbs(100);

        assertThatThrownBy(() -> carbs.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectAdditionThatExceedsMaximumCarbs() {

        Carbs carbs = new Carbs(1000);

        assertThatThrownBy(() ->
                carbs.add(new Carbs(1))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConvertCarbsToCalories() {

        Carbs carbs = new Carbs(100);

        assertThat(carbs.toCalories())
                .isEqualTo(400);
    }

    @Test
    void shouldReturnTrueWhenCarbsAreZero() {

        assertThat(new Carbs(0).isZero())
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenCarbsAreNotZero() {

        assertThat(new Carbs(100).isZero())
                .isFalse();
    }

    @Test
    void shouldReturnDisplayString() {

        assertThat(new Carbs(150).toDisplayString())
                .isEqualTo("150 g");
    }

}