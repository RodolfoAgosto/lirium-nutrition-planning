package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Range-level summary of meal adherence")
public record AdherenceSummaryDTO(
    @Schema(description = "Days in the range", example = "31") int totalDays,
    @Schema(description = "Days with a daily record", example = "13") int recordedDays,
    @Schema(description = "Meals expected over the whole range", example = "155") int expectedMeals,
    @Schema(description = "Meals followed as planned over the range", example = "57")
        int followedMeals,
    @Schema(
            description =
                "followedMeals / expectedMeals over the whole range, in percent. Mixes logging"
                    + " consistency and compliance.",
            example = "36.8")
        double adherence,
    @Schema(
            description =
                "Same ratio over recorded days only: compliance when the patient does log. Null"
                    + " when no day was recorded.",
            example = "87.7",
            nullable = true)
        Double adherenceOnRecordedDays) {}
