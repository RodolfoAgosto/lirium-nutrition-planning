package com.lirium.nutrition.dto.response;

import java.time.LocalDate;
import java.util.List;

public record NutritionComparisonReportDTO(
        LocalDate from,
        LocalDate to,
        List<DailyNutritionComparisonDTO> days
) {}
