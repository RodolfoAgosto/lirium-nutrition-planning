package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;

import java.time.LocalDate;

public record NutritionPlanSummaryDTO(
        Long id,
        String name,
        PlanStatus status,
        GoalType targetGoal,
        int dailyCalories,
        LocalDate startDate,
        LocalDate endDate
) {}