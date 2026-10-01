package com.lirium.nutrition.dto.response;

public record NutritionComparisonSummaryDTO(
    int totalDays,
    int recordedDays,
    NutritionScoreDTO averageScore,
    NutrientsDTO averageConsumed) {}
