package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.util.List;

public record DailyPlanCreateRequestDTO(

        @NotNull(message = "Day is required")
        DayOfWeek day,

        @NotNull(message = "Nutrition plan id is required")
        Long nutritionPlanId,

        List<Long> mealIds

) {}