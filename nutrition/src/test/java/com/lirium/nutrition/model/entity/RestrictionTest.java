package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RestrictionTest {


    @Test
    void shouldCreateRestriction() {

        Restriction restriction =
                Restriction.builder()
                        .code("GLUTEN")
                        .name("Gluten intolerance")
                        .category(RestrictionCategory.INTOLERANCES)
                        .description("Avoid gluten products")
                        .excludedTags(Set.of(FoodTag.GLUTEN))
                        .build();


        assertThat(restriction.getCode())
                .isEqualTo("GLUTEN");

        assertThat(restriction.getName())
                .isEqualTo("Gluten intolerance");

        assertThat(restriction.getCategory())
                .isEqualTo(RestrictionCategory.INTOLERANCES);

        assertThat(restriction.getExcludedTags())
                .containsExactly(FoodTag.GLUTEN);
    }


    @Test
    void shouldInitializeExcludedTags() {

        Restriction restriction =
                new Restriction();


        assertThat(restriction.getExcludedTags())
                .isNotNull();

        assertThat(restriction.getExcludedTags())
                .isEmpty();
    }


    @Test
    void shouldAllowMultipleExcludedTags() {

        Restriction restriction =
                Restriction.builder()
                        .code("VEGAN")
                        .name("Vegan restriction")
                        .category(RestrictionCategory.DIETARY)
                        .description("Avoid animal products")
                        .excludedTags(
                                Set.of(
                                        FoodTag.MEAT,
                                        FoodTag.NUTS
                                )
                        )
                        .build();


        assertThat(restriction.getExcludedTags())
                .containsExactlyInAnyOrder(
                        FoodTag.MEAT,
                        FoodTag.NUTS
                );
    }


    @Test
    void shouldConsiderSameIdEqual() {

        Restriction r1 = new Restriction();
        Restriction r2 = new Restriction();

        // simulando entidades persistidas
        setId(r1, 1L);
        setId(r2, 1L);


        assertThat(r1)
                .isEqualTo(r2);
    }


    private void setId(Restriction restriction, Long id) {

        try {
            var field = Restriction.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(restriction, id);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}