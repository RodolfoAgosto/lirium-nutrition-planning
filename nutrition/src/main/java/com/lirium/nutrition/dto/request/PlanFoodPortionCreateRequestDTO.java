package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.validation.constraints.*;

public record PlanFoodPortionCreateRequestDTO(

        @NotNull(message = "Meal id is required")
        Long mealId,

        @NotNull(message = "Food id is required")
        Long foodId,

        @NotNull(message = "Grams is required")
        @Min(value = 1, message = "Grams must be positive")
        @Max(value = 5000, message = "Grams too large")
        Integer grams,

        Double quantity,

        MeasureUnit unit


) {}