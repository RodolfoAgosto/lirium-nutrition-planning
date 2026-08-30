package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import jakarta.validation.constraints.*;
import java.util.Set;

public record FoodCreateRequestDTO(
    @NotBlank(message = "Name is required") @Size(max = 120, message = "Name max length is 120")
        String name,
    @NotNull(message = "Calories are required")
        @Min(value = 0, message = "Calories cannot be negative")
        @Max(value = 1000, message = "Calories per 100g cannot exceed 1000")
        Integer caloriesPer100g,
    @NotNull(message = "Protein is required")
        @Min(value = 0, message = "Protein cannot be negative")
        @Max(value = 100, message = "Protein cannot exceed 100g")
        Integer proteinPer100g,
    @NotNull(message = "Carbs are required")
        @Min(value = 0, message = "Carbs cannot be negative")
        @Max(value = 100, message = "Carbs cannot exceed 100g")
        Integer carbsPer100g,
    @NotNull(message = "Fat is required")
        @Min(value = 0, message = "Fat cannot be negative")
        @Max(value = 100, message = "Fat cannot exceed 100g")
        Integer fatPer100g,
    @NotNull(message = "Category is required") FoodCategory category,
    Set<MealType> suitableFor,
    @Size(max = 30, message = "Cannot exceed 30 tags") Set<@NotNull FoodTag> tags) {}
