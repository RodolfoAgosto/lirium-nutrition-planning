package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Daily amount of energy and macronutrients")
public record NutrientsDTO(
    @Schema(description = "Energy in kcal", example = "2160") int calories,
    @Schema(description = "Protein in grams", example = "102") int protein,
    @Schema(description = "Carbohydrates in grams", example = "276") int carbs,
    @Schema(description = "Fat in grams", example = "72") int fat) {}
