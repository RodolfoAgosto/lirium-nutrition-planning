package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;

import java.util.List;

public class PlanMealMapper {

    private PlanMealMapper() {}

    /* ========= ENTITY → RESPONSE ========= */

    public static PlanMealResponseDTO toResponse(PlanMeal entity) {

        List<PlanFoodPortionSummaryDTO> foods =
                entity.getFoodPortions()
                        .stream()
                        .map(PlanFoodPortionMapper::toSummary)
                        .toList();

        return new PlanMealResponseDTO(
                entity.getId(),
                entity.getType().name(),
                entity.getDailyPlan().getId(),
                foods
        );
    }

    public static PlanMealSummaryDTO toSummary(PlanMeal entity) {
        return new PlanMealSummaryDTO(
                entity.getId(),
                entity.getType().name()
        );
    }

    /* ========= CREATE DTO → ENTITY ========= */

    public static PlanMeal toEntity(
            PlanMealCreateRequestDTO dto,
            DailyPlan dailyPlan
    ) {
        MealType type = MealType.valueOf(dto.type().toUpperCase());
        return PlanMeal.of(type, dailyPlan);
    }

    /* ========= UPDATE ========= */

    public static void updateEntity(
            PlanMeal entity,
            PlanMealUpdateRequestDTO dto
    ) {
        if (dto.type() != null) {
            MealType type = MealType.valueOf(dto.type().toUpperCase());
            // entity.changeType(type); ← si permitís cambiar
        }
    }
}