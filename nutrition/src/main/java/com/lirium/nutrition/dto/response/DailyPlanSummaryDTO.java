package com.lirium.nutrition.dto.response;

import java.time.DayOfWeek;

public record DailyPlanSummaryDTO(
        Long id,
        DayOfWeek day
) {}