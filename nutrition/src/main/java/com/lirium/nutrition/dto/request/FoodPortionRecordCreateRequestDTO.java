package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.validation.constraints.*;

public record FoodPortionRecordCreateRequestDTO(

        @NotNull(message = "Meal id is required")
        Long mealId,

        @NotNull(message = "Food id is required")
        Long foodId,

        Double quantity,

        MeasureUnit unit

) {}