package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class NutritionPlanTemplateTest {


    @Test
    void shouldCreateValidTemplate() {

        NutritionPlanTemplate template = NutritionPlanTemplate.of(
                "Low Carb",
                "Plan low carb",
                GoalType.WEIGHT_LOSS,
                40,
                40,
                20,
                Set.of(FoodTag.NUTS)
        );


        assertThat(template.getName())
                .isEqualTo("Low Carb");

        assertThat(template.getTargetGoal())
                .isEqualTo(GoalType.WEIGHT_LOSS);

        assertThat(template.getExcludedTags())
                .contains(FoodTag.NUTS);
    }


    @Test
    void shouldRejectBlankName() {

        assertThatThrownBy(() ->
                NutritionPlanTemplate.of(
                        "",
                        "Description",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        20,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldRejectBlankDescription() {

        assertThatThrownBy(() ->
                NutritionPlanTemplate.of(
                        "Template",
                        "",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        20,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldRejectNullGoal() {

        assertThatThrownBy(() ->
                NutritionPlanTemplate.of(
                        "Template",
                        "Description",
                        null,
                        40,
                        40,
                        20,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldRejectNegativeMacros() {

        assertThatThrownBy(() ->
                NutritionPlanTemplate.of(
                        "Template",
                        "Description",
                        GoalType.WEIGHT_LOSS,
                        -1,
                        50,
                        51,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldRejectMacrosNotAdding100() {

        assertThatThrownBy(() ->
                NutritionPlanTemplate.of(
                        "Template",
                        "Description",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        30,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldUpdateTemplateFields() {

        NutritionPlanTemplate template = NutritionPlanTemplate.of(
                "Old",
                "Old description",
                GoalType.WEIGHT_LOSS,
                40,
                40,
                20,
                null
        );


        template.update(
                "New",
                "New description",
                GoalType.MUSCLE_GAIN,
                Set.of(FoodTag.GLUTEN)
        );


        assertThat(template.getName())
                .isEqualTo("New");

        assertThat(template.getDescription())
                .isEqualTo("New description");

        assertThat(template.getTargetGoal())
                .isEqualTo(GoalType.MUSCLE_GAIN);

        assertThat(template.getExcludedTags())
                .containsExactly(FoodTag.GLUTEN);
    }


    @Test
    void shouldUpdateMacrosWhenSumIs100() {

        NutritionPlanTemplate template = NutritionPlanTemplate.of(
                "Template",
                "Description",
                GoalType.WEIGHT_LOSS,
                40,
                40,
                20,
                null
        );


        template.updateMacros(30, 50, 20);


        assertThat(template.getProteinPercentage())
                .isEqualTo(30);

        assertThat(template.getCarbPercentage())
                .isEqualTo(50);

        assertThat(template.getFatPercentage())
                .isEqualTo(20);
    }


    @Test
    void shouldRejectInvalidMacroUpdate() {

        NutritionPlanTemplate template = NutritionPlanTemplate.of(
                "Template",
                "Description",
                GoalType.WEIGHT_LOSS,
                40,
                40,
                20,
                null
        );


        assertThatThrownBy(() ->
                template.updateMacros(30,30,30)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}