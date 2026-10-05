package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "One day of the nutrition comparison report")
public record DailyNutritionComparisonDTO(
    @Schema(description = "Day being compared", example = "2026-09-15") LocalDate date,
    @Schema(description = "Whether the patient recorded anything that day") boolean hasRecord,
    @Schema(description = "What the active plan prescribes for this day of the week")
        NutrientsDTO planned,
    @Schema(
            description =
                "What the patient recorded. Null when hasRecord is false: no data, not zero intake.")
        NutrientsDTO consumed,
    @Schema(
            description =
                "Closeness of consumed to planned. Null when there is no record or nothing was"
                    + " planned for the day.",
            nullable = true)
        NutritionScoreDTO score) {}
