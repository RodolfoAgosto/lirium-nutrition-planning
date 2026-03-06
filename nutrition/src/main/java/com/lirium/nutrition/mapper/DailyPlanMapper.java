package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

public class DailyPlanMapper {

    private DailyPlanMapper() {}

    /* === ENTITY -> RESPONSE === */

    public static DailyPlanResponseDTO toResponse(DailyPlan entity) {

        List<PlanMealSummaryDTO> meals = entity.getMeals()
                .stream()
                .map(PlanMealMapper::toSummary)
                .toList();

        return new DailyPlanResponseDTO(
                entity.getId(),
                entity.getDayOfWeek(),
                entity.getNutritionPlan().getId(),
                meals
        );
    }

    public static DailyPlanSummaryDTO toSummary(DailyPlan entity) {
        return new DailyPlanSummaryDTO(
                entity.getId(),
                entity.getDayOfWeek()
        );
    }

    /* === CREATE DTO -> ENTITY === */

    public static DailyPlan toEntity(
            DailyPlanCreateRequestDTO dto,
            NutritionPlan nutritionPlan
    ) {

        DayOfWeek day = DayOfWeek.valueOf(dto.day().toUpperCase());

        return DailyPlan.of(day, nutritionPlan);
    }
}