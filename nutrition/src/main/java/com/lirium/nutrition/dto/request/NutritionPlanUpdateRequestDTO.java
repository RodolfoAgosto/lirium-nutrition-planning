package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.GoalType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record NutritionPlanUpdateRequestDTO(

        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        LocalDate startDate,
        LocalDate endDate,

        GoalType targetGoal,

        @Min(1) @Max(10000)
        Integer dailyCalories,

        @Min(0) @Max(500)
        Integer proteinGrams,

        @Min(0) @Max(500)
        Integer carbGrams,

        @Min(0) @Max(300)
        Integer fatGrams

) {}