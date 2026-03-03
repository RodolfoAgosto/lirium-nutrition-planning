package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.FoodTag;

import java.util.Set;

public record NutritionPlanTemplateResponseDTO(

        Long id,
        String name,
        String description,
        GoalType targetGoal,
        int proteinPercentage,
        int carbPercentage,
        int fatPercentage,
        Set<FoodTag> excludedTags

) {}
