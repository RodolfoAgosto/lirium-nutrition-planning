package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request payload for adding a food portion to a meal record")
public record FoodPortionAddRequestDTO(
    @Schema(description = "ID of the food item to add", example = "15")
        @NotNull(message = "Food ID is required")
        Long foodId,
    @Schema(description = "Consumed quantity", example = "150.0")
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Double quantity,
    @Schema(description = "Measurement unit", example = "GRAM")
        @NotNull(message = "Measure unit is required")
        MeasureUnit unit) {}
