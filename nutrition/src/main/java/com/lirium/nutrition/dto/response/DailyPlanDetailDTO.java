package com.lirium.nutrition.dto.response;

import java.time.DayOfWeek;
import java.util.List;

public record DailyPlanDetailDTO(
    Long id, DayOfWeek dayOfWeek, NutrientsDTO totals, List<PlanMealDetailDTO> meals) {}
