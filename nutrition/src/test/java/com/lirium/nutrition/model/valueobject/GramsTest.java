package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GramsTest {

    @Test
    void shouldCreateGrams() {

        Grams grams = new Grams(100);

        assertThat(grams.amount()).isEqualTo(100);
    }

    @Test
    void shouldAcceptZeroGrams() {

        Grams grams = new Grams(0);

        assertThat(grams.amount()).isZero();
    }

    @Test
    void shouldRejectNegativeGrams() {

        assertThatThrownBy(() -> new Grams(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

}