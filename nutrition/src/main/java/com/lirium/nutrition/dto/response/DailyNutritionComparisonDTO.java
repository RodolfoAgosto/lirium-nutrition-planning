package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record DailyNutritionComparisonDTO(
    LocalDate date,
    boolean hasRecord,
    NutrientsDTO planned,
    NutrientsDTO consumed,
    NutritionScoreDTO score) {}
