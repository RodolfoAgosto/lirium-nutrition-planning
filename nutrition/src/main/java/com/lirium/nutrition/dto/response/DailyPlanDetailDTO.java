package com.lirium.nutrition.dto.response;

import java.time.DayOfWeek;
import java.util.List;

public record DailyPlanDetailDTO(
        DayOfWeek dayOfWeek,
        List<PlanMealDetailDTO> meals
) {}
