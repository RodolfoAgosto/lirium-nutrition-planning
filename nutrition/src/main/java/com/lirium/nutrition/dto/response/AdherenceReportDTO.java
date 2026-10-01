package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "How many planned meals the patient followed, day by day, for a date range")
public record AdherenceReportDTO(
    @Schema(description = "Start date", example = "2026-08-31") LocalDate from,
    @Schema(description = "End date", example = "2026-09-30") LocalDate to,
    @Schema(description = "Totals and percentages for the whole range") AdherenceSummaryDTO summary,
    @Schema(description = "One entry per day of the range") List<DailyAdherenceDTO> days) {}
