package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NutritionPlanTemplateMapperTest {

    @Test
    void shouldMapCreateDtoToEntity() {

        NutritionPlanTemplateCreateRequestDTO dto =
                new NutritionPlanTemplateCreateRequestDTO(
                        "Weight Loss",
                        "Low calorie template",
                        GoalType.WEIGHT_LOSS,
                        40,
                        35,
                        25,
                        Set.of(FoodTag.HONEY, FoodTag.NUTS)
                );

        NutritionPlanTemplate entity =
                NutritionPlanTemplateMapper.toEntity(dto);

        assertThat(entity.getName()).isEqualTo("Weight Loss");
        assertThat(entity.getDescription()).isEqualTo("Low calorie template");
        assertThat(entity.getTargetGoal()).isEqualTo(GoalType.WEIGHT_LOSS);
        assertThat(entity.getProteinPercentage()).isEqualTo(40);
        assertThat(entity.getCarbPercentage()).isEqualTo(35);
        assertThat(entity.getFatPercentage()).isEqualTo(25);
        assertThat(entity.getExcludedTags())
                .containsExactlyInAnyOrder(
                        FoodTag.HONEY,
                        FoodTag.NUTS
                );
    }

    @Test
    void shouldMapCreateDtoWithNullTags() {

        NutritionPlanTemplateCreateRequestDTO dto =
                new NutritionPlanTemplateCreateRequestDTO(
                        "Maintenance",
                        "Standard template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        45,
                        25,
                        null
                );

        NutritionPlanTemplate entity =
                NutritionPlanTemplateMapper.toEntity(dto);

        assertThat(entity.getExcludedTags()).isEmpty();
    }

    @Test
    void shouldMapEntityToResponse() {

        NutritionPlanTemplate entity =
                NutritionPlanTemplate.of(
                        "Muscle Gain",
                        "High protein",
                        GoalType.MUSCLE_GAIN,
                        40,
                        40,
                        20,
                        new HashSet<>(Set.of(
                                FoodTag.HONEY,
                                FoodTag.NUTS
                        ))
                );

        NutritionPlanTemplateResponseDTO dto =
                NutritionPlanTemplateMapper.toResponse(entity);

        assertThat(dto.name()).isEqualTo("Muscle Gain");
        assertThat(dto.description()).isEqualTo("High protein");
        assertThat(dto.targetGoal()).isEqualTo(GoalType.MUSCLE_GAIN);
        assertThat(dto.proteinPercentage()).isEqualTo(40);
        assertThat(dto.carbPercentage()).isEqualTo(40);
        assertThat(dto.fatPercentage()).isEqualTo(20);
        assertThat(dto.excludedTags())
                .containsExactlyInAnyOrder(
                        FoodTag.HONEY,
                        FoodTag.NUTS
                );
    }

    @Test
    void shouldMapEntityToSummary() {

        NutritionPlanTemplate entity =
                NutritionPlanTemplate.of(
                        "Keto",
                        "Low carb",
                        GoalType.WEIGHT_LOSS,
                        30,
                        10,
                        60,
                        new HashSet<>()
                );

        NutritionPlanTemplateSummaryDTO dto =
                NutritionPlanTemplateMapper.toSummary(entity);

        assertThat(dto.name()).isEqualTo("Keto");
        assertThat(dto.targetGoal()).isEqualTo(GoalType.WEIGHT_LOSS);
    }

}