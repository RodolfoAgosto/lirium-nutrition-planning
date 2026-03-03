package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;

public record FoodPortionRecordCreateRequestDTO(

        @NotNull(message = "Meal id is required")
        Long mealId,

        @NotNull(message = "Food id is required")
        Long foodId,

        @NotNull(message = "Grams is required")
        @Min(value = 1, message = "Grams must be positive")
        @Max(value = 5000, message = "Grams too large")
        Integer grams

) {}