package com.lirium.nutrition.dto.request;

import java.util.Set;
import jakarta.validation.constraints.*;

public record FoodUpdateRequestDTO(

        @Size(max = 120)
        String name,

        @Min(0)
        @Max(5000)
        Integer caloriesPer100g,

        @Min(0)
        @Max(100)
        Integer proteinPer100g,

        @Min(0)
        @Max(100)
        Integer carbsPer100g,

        @Min(0)
        @Max(100)
        Integer fatPer100g,

        Set<@NotBlank String> tags

) {}