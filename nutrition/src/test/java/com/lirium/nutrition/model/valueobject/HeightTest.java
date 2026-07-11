package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeightTest {

    @Test
    void shouldCreateHeight() {

        Height height = new Height(170);

        assertThat(height.cm()).isEqualTo(170);
    }

    @Test
    void shouldAcceptMinimumHeight() {

        Height height = new Height(30);

        assertThat(height.cm()).isEqualTo(30);
    }

    @Test
    void shouldAcceptMaximumHeight() {

        Height height = new Height(250);

        assertThat(height.cm()).isEqualTo(250);
    }

    @Test
    void shouldRejectHeightBelowMinimum() {

        assertThatThrownBy(() -> new Height(29))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectHeightAboveMaximum() {

        assertThatThrownBy(() -> new Height(251))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateHeightUsingFactoryMethod() {

        Height height = Height.of(180);

        assertThat(height.cm()).isEqualTo(180);
    }

    @Test
    void shouldReturnDisplayString() {

        Height height = new Height(175);

        assertThat(height.toDisplayString())
                .isEqualTo("175 cm");
    }

}