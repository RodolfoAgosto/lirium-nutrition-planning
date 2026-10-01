package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Range-level summary. Days without data are excluded from the averages.")
public record NutritionComparisonSummaryDTO(
    @Schema(description = "Days in the effective range", example = "31") int totalDays,
    @Schema(description = "Days with a daily record", example = "13") int recordedDays,
    @Schema(
            description = "Average daily score over scored days. Null when no day has a score.",
            nullable = true)
        NutritionScoreDTO averageScore,
    @Schema(
            description = "Average daily intake over recorded days. Null when no day was recorded.",
            nullable = true)
        NutrientsDTO averageConsumed) {}
