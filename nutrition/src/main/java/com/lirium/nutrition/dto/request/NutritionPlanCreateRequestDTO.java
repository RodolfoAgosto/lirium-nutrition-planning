package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.GoalType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record NutritionPlanCreateRequestDTO(

        @NotBlank @Size(max = 120)
        String name,

        @NotBlank @Size(max = 500)
        String description,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        GoalType targetGoal,

        @Min(1) @Max(10000)
        int dailyCalories,

        @Min(0) @Max(500)
        int proteinGrams,

        @Min(0) @Max(500)
        int carbGrams,

        @Min(0) @Max(300)
        int fatGrams

) {}