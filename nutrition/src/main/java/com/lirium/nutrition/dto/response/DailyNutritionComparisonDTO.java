package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record DailyNutritionComparisonDTO(
        LocalDate date,
        int targetCalories,
        int consumedCalories,
        int targetProtein,
        int consumedProtein,
        int targetCarbs,
        int consumedCarbs,
        int targetFat,
        int consumedFat,
        double adherencePercentage
) {}
