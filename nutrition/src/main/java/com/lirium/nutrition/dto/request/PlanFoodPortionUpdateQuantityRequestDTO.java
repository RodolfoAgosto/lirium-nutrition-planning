package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PlanFoodPortionUpdateQuantityRequestDTO (
        @NotNull
        @DecimalMin("0.01")
        Double quantity
) {}