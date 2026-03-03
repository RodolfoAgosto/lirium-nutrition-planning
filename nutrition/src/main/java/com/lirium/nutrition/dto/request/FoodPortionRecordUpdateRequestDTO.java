package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;

public record FoodPortionRecordUpdateRequestDTO(

        Long foodId,

        @Min(1)
        @Max(5000)
        Integer grams

) {}