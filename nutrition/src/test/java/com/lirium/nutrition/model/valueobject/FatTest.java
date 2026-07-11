package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FatTest {

    @Test
    void shouldCreateFat() {

        Fat fat = new Fat(50);

        assertThat(fat.amount()).isEqualTo(50);
    }

    @Test
    void shouldAcceptMinimumFat() {

        Fat fat = new Fat(0);

        assertThat(fat.amount()).isZero();
    }

    @Test
    void shouldAcceptMaximumFat() {

        Fat fat = new Fat(2000);

        assertThat(fat.amount()).isEqualTo(2000);
    }

    @Test
    void shouldRejectNegativeFat() {

        assertThatThrownBy(() -> new Fat(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectFatGreaterThanMaximum() {

        assertThatThrownBy(() -> new Fat(2001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddFat() {

        Fat result =
                new Fat(50)
                        .add(new Fat(25));

        assertThat(result.amount()).isEqualTo(75);
    }

    @Test
    void shouldAddZeroFat() {

        Fat result =
                new Fat(50)
                        .add(new Fat(0));

        assertThat(result.amount()).isEqualTo(50);
    }

    @Test
    void shouldRejectNullFatWhenAdding() {

        Fat fat = new Fat(50);

        assertThatThrownBy(() -> fat.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectAdditionThatExceedsMaximumFat() {

        Fat fat = new Fat(2000);

        assertThatThrownBy(() ->
                fat.add(new Fat(1))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConvertFatToCalories() {

        Fat fat = new Fat(10);

        assertThat(fat.toCalories())
                .isEqualTo(90.0);
    }

    @Test
    void shouldReturnDisplayString() {

        assertThat(new Fat(75).toDisplayString())
                .isEqualTo("75 g");
    }

}