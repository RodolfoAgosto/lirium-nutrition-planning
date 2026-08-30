package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Set;

public record NutritionPlanTemplateCreateRequestDTO(
    @Schema(description = "Name of the template", example = "High Protein Weight Loss Template")
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name cannot exceed 120 characters")
        String name,
    @Schema(
            description = "Detailed description of the template",
            example = "Balanced template with 40% protein split designed for cutting.")
        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,
    @Schema(description = "Target goal for this template", example = "WEIGHT_LOSS")
        @NotNull(message = "Target goal is required")
        GoalType targetGoal,
    @Schema(description = "Protein percentage (0-100)", example = "40")
        @NotNull(message = "Protein percentage is required")
        @Min(value = 0, message = "Protein percentage cannot be negative")
        @Max(value = 100, message = "Protein percentage cannot exceed 100")
        int proteinPercentage,
    @Schema(description = "Carbohydrate percentage (0-100)", example = "30")
        @NotNull(message = "Carb percentage is required")
        @Min(value = 0, message = "Carb percentage cannot be negative")
        @Max(value = 100, message = "Carb percentage cannot exceed 100")
        int carbPercentage,
    @Schema(description = "Fat percentage (0-100)", example = "30")
        @NotNull(message = "Fat percentage is required")
        @Min(value = 0, message = "Fat percentage cannot be negative")
        @Max(value = 100, message = "Fat percentage cannot exceed 100")
        int fatPercentage,
    @Schema(
            description = "Food tags to automatically exclude from this template",
            example = "[\"GLUTEN\", \"LACTOSE\"]")
        Set<FoodTag> excludedTags) {}
