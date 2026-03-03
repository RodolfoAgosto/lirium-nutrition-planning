package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;

import java.util.HashSet;

public class NutritionPlanTemplateMapper {

    private NutritionPlanTemplateMapper() {}

    /* === CREATE === */
    public static NutritionPlanTemplate toEntity(
            NutritionPlanTemplateCreateRequestDTO dto
    ) {

        return NutritionPlanTemplate.of(
                dto.name(),
                dto.description(),
                dto.targetGoal(),
                dto.proteinPercentage(),
                dto.carbPercentage(),
                dto.fatPercentage(),
                dto.excludedTags() != null
                        ? new HashSet<>(dto.excludedTags())
                        : new HashSet<>()
        );
    }

    /* === RESPONSE === */
    public static NutritionPlanTemplateResponseDTO toResponse(
            NutritionPlanTemplate entity
    ) {

        return new NutritionPlanTemplateResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTargetGoal(),
                entity.getProteinPercentage(),
                entity.getCarbPercentage(),
                entity.getFatPercentage(),
                entity.getExcludedTags()
        );
    }

    /* === SUMMARY === */
    public static NutritionPlanTemplateSummaryDTO toSummary(
            NutritionPlanTemplate entity
    ) {

        return new NutritionPlanTemplateSummaryDTO(
                entity.getId(),
                entity.getName(),
                entity.getTargetGoal()
        );
    }
}