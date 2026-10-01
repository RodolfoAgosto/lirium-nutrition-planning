package com.lirium.nutrition.dto.response;

public record NutritionScoreDTO(
    double calories, double protein, double carbs, double fat, double overall) {}
