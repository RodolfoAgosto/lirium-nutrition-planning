package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.FoodTag;
import jakarta.validation.constraints.*;

import java.util.Set;

public record NutritionPlanTemplateCreateRequestDTO(

        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 500)
        String description,

        @NotNull
        GoalType targetGoal,

        @Min(0) @Max(100)
        int proteinPercentage,

        @Min(0) @Max(100)
        int carbPercentage,

        @Min(0) @Max(100)
        int fatPercentage,

        Set<FoodTag> excludedTags

) {}