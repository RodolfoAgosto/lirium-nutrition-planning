package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "How close the intake was to the planned intake, from 0 to 100 per nutrient. 100 is an"
            + " exact match; the score drops equally for under- and over-eating.")
public record NutritionScoreDTO(
    @Schema(description = "Calorie closeness (0-100)", example = "95.1") double calories,
    @Schema(description = "Protein closeness (0-100)", example = "88.2") double protein,
    @Schema(description = "Carbohydrate closeness (0-100)", example = "84.4") double carbs,
    @Schema(description = "Fat closeness (0-100)", example = "91.9") double fat,
    @Schema(
            description =
                "Weighted overall: calories 40%, each macro 20%. Illustrative weights, not"
                    + " clinical guidance.",
            example = "91.0")
        double overall) {}
