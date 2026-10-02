package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "One day of the meal adherence report")
public record DailyAdherenceDTO(
    @Schema(description = "Day being measured", example = "2026-09-20") LocalDate date,
    @Schema(description = "Whether the patient recorded anything that day") boolean hasRecord,
    @Schema(
            description = "Meals prescribed by the plan in effect that day; 0 when no plan applied",
            example = "5")
        int expectedMeals,
    @Schema(
            description = "Meals eaten as planned, without changes. Null when hasRecord is false.",
            example = "4",
            nullable = true)
        Integer followedMeals,
    @Schema(
            description = "Meals recorded with changes. Null when hasRecord is false.",
            example = "1",
            nullable = true)
        Integer modifiedMeals) {}
