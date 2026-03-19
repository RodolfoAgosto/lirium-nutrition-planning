package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import jakarta.validation.constraints.*;
import java.util.Set;

public record FoodCreateRequestDTO(

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name max length is 120")
        String name,

        @NotNull
        @Min(0)
        @Max(5000)
        Integer caloriesPer100g,

        @NotNull
        @Min(0)
        @Max(100)
        Integer proteinPer100g,

        @NotNull
        @Min(0)
        @Max(100)
        Integer carbsPer100g,

        @NotNull
        @Min(0)
        @Max(100)
        Integer fatPer100g,

        FoodCategory category,

        Set<MealType> suitableFor,

        Set<@NotBlank String> tags

) {}