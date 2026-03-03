package com.lirium.nutrition.dto.response;

import java.time.DayOfWeek;
import java.util.List;

public record DailyPlanResponseDTO(
        Long id,
        DayOfWeek day,
        Long nutritionPlanId,
        List<PlanMealSummaryDTO> meals
) {}