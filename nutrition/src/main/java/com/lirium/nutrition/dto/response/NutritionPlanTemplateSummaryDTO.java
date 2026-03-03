package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;

public record NutritionPlanTemplateSummaryDTO(

        Long id,
        String name,
        GoalType targetGoal

) {}