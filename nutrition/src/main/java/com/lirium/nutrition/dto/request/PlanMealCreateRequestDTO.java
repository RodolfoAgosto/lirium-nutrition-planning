package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlanMealCreateRequestDTO(
    @NotNull(message = "Meal type is required") MealType type,
    @NotNull(message = "Daily plan ID is required")
        @Positive(message = "Daily plan ID must be positive")
        Long dailyPlanId) {}
