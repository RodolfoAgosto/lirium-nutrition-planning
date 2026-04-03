package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;

import java.util.List;

public record NutritionPlanDetailDTO(
        Long id,
        String name,
        String description,
        PlanStatus status,
        GoalType targetGoal,
        int dailyCalories,
        int proteinGrams,
        int carbGrams,
        int fatGrams,
        List<DailyPlanDetailDTO> week
) {}
