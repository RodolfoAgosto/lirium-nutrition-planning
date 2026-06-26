package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.validation.constraints.NotNull;

public record FoodPortionAddRequestDTO(
        @NotNull Long foodId,
        @NotNull Double quantity,
        @NotNull MeasureUnit unit
) {}