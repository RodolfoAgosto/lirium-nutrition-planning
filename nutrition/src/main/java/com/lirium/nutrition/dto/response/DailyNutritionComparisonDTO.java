package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record DailyNutritionComparisonDTO(
    LocalDate date,
    int targetCalories,
    int plannedCalories,
    int consumedCalories,
    int targetProtein,
    int plannedProtein,
    int consumedProtein,
    int targetCarbs,
    int plannedCarbs,
    int consumedCarbs,
    int targetFat,
    int plannedFat,
    int consumedFat,
    double adherencePercentage,
    boolean hasRecord) {}
