package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FoodPortionAddRequestDTO(
        @NotNull Long foodId,
        @NotNull @Positive Double quantity,
        @NotNull MeasureUnit unit
) {}