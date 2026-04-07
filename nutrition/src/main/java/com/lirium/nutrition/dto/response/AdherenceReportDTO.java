package com.lirium.nutrition.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AdherenceReportDTO(
        LocalDate from,
        LocalDate to,
        int totalExpectedMeals,
        int totalRecordedMeals,
        double adherencePercentage,
        List<DailyAdherenceDTO> daily
) {}