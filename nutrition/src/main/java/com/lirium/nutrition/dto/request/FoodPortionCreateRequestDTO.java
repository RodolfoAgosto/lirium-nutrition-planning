package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.validation.constraints.*;

public record FoodPortionCreateRequestDTO(
    @NotNull Long foodId, Double quantity, MeasureUnit unit) {}
