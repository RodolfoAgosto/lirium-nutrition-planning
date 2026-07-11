package com.lirium.nutrition.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProteinTest {

    @Test
    void shouldCreateProtein() {

        Protein protein = new Protein(100);

        assertThat(protein.grams()).isEqualTo(100);
    }

    @Test
    void shouldAcceptZeroProtein() {

        Protein protein = new Protein(0);

        assertThat(protein.grams()).isZero();
    }

    @Test
    void shouldRejectNegativeProtein() {

        assertThatThrownBy(() -> new Protein(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddProtein() {

        Protein result =
                new Protein(100)
                        .add(new Protein(50));

        assertThat(result.grams()).isEqualTo(150);
    }

    @Test
    void shouldAddZeroProtein() {

        Protein result =
                new Protein(100)
                        .add(new Protein(0));

        assertThat(result.grams()).isEqualTo(100);
    }

    @Test
    void shouldRejectNullProteinWhenAdding() {

        Protein protein = new Protein(100);

        assertThatThrownBy(() -> protein.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldConvertProteinToCalories() {

        Protein protein = new Protein(100);

        assertThat(protein.toCalories())
                .isEqualTo(400.0);
    }

    @Test
    void shouldReturnDisplayString() {

        Protein protein = new Protein(75);

        assertThat(protein.toDisplayString())
                .isEqualTo("75 g");
    }

}