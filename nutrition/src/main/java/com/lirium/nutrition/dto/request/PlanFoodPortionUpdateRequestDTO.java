package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;

public record PlanFoodPortionUpdateRequestDTO(

        @Min(1)
        @Max(5000)
        Double quantity

) {}