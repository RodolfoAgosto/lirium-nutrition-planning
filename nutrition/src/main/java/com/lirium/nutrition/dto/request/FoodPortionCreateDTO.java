package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;

public record FoodPortionCreateDTO(

        @NotNull
        Long foodId,

        @NotNull
        @Min(1)
        @Max(2000)
        Integer grams
) {}