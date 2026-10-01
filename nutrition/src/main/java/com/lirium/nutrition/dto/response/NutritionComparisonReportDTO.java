package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(
    description =
        "Compares what the patient recorded against what the active plan prescribes, day by"
            + " day, for a date range.")
public record NutritionComparisonReportDTO(
    @Schema(
            description =
                "Effective start date: the requested one, or the active plan's start date if"
                    + " later",
            example = "2026-08-31")
        LocalDate from,
    @Schema(description = "End date", example = "2026-09-30") LocalDate to,
    @Schema(description = "The plan's daily targets, returned once as reference")
        NutrientsDTO targets,
    @Schema(description = "Averages and counts for the whole range")
        NutritionComparisonSummaryDTO summary,
    @Schema(description = "One entry per day of the effective range")
        List<DailyNutritionComparisonDTO> days) {}
