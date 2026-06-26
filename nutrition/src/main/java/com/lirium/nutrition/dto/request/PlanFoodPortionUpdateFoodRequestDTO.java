package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PlanFoodPortionUpdateFoodRequestDTO(
        @NotNull
        Long foodId,
        @NotNull
        @DecimalMin("0.01")
        Double quantity
) {}