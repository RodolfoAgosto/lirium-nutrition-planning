package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.FoodTag;
import jakarta.validation.constraints.*;

import java.util.Set;

public record NutritionPlanTemplateUpdateRequestDTO(

        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        GoalType targetGoal,

        @Min(0) @Max(100)
        Integer proteinPercentage,

        @Min(0) @Max(100)
        Integer carbPercentage,

        @Min(0) @Max(100)
        Integer fatPercentage,

        Set<FoodTag> excludedTags

) {}