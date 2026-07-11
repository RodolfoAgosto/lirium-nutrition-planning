package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightTest {

    @Test
    void shouldCreateWeight() {

        Weight weight = new Weight(70000);

        assertThat(weight.grams()).isEqualTo(70000);
    }

    @Test
    void shouldAcceptMinimumWeight() {

        Weight weight = new Weight(300);

        assertThat(weight.grams()).isEqualTo(300);
    }

    @Test
    void shouldAcceptMaximumWeight() {

        Weight weight = new Weight(300000);

        assertThat(weight.grams()).isEqualTo(300000);
    }

    @Test
    void shouldRejectWeightBelowMinimum() {

        assertThatThrownBy(() -> new Weight(299))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectWeightAboveMaximum() {

        assertThatThrownBy(() -> new Weight(300001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateWeightUsingFactoryMethod() {

        Weight weight = Weight.of(75000);

        assertThat(weight.grams()).isEqualTo(75000);
    }

    @Test
    void shouldConvertWeightToKilograms() {

        Weight weight = new Weight(72500);

        assertThat(weight.toKg())
                .isEqualTo(72.5);
    }

    @Test
    void shouldReturnDisplayString() {

        Weight weight = new Weight(72500);

        assertThat(weight.toDisplayString())
                .isEqualTo("72,50 kg");

    }

}