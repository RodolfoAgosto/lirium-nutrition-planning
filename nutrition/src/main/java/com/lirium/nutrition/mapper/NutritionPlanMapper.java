package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.NutritionPlan;

public class NutritionPlanMapper {

    private NutritionPlanMapper() {}

    // === CREATE ===
    public static NutritionPlan toEntity(NutritionPlanCreateRequestDTO dto) {

        return NutritionPlan.of(
                dto.name(),
                dto.description(),
                dto.startDate(),
                dto.endDate(),
                dto.targetGoal(),
                dto.dailyCalories(),
                dto.proteinGrams(),
                dto.carbGrams(),
                dto.fatGrams()
        );
    }

    // === UPDATE ===
    public static void updateEntity(
            NutritionPlan plan,
            NutritionPlanUpdateRequestDTO dto
    ){
        plan.update(
                dto.name(),
                dto.description(),
                dto.startDate(),
                dto.endDate(),
                dto.targetGoal(),
                dto.dailyCalories(),
                dto.proteinGrams(),
                dto.carbGrams(),
                dto.fatGrams()
        );
    }

    // ========= RESPONSE =========
    public static NutritionPlanResponseDTO toResponse(NutritionPlan plan) {

        return new NutritionPlanResponseDTO(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getTargetGoal(),
                plan.getDailyCalories(),
                plan.getProteinGrams(),
                plan.getCarbGrams(),
                plan.getFatGrams(),
                plan.getWeek().size()
        );
    }

    // ========= SUMMARY =========
    public static NutritionPlanSummaryDTO toSummary(NutritionPlan plan) {

        return new NutritionPlanSummaryDTO(
                plan.getId(),
                plan.getName(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getTargetGoal(),
                plan.getDailyCalories()
        );
    }
}