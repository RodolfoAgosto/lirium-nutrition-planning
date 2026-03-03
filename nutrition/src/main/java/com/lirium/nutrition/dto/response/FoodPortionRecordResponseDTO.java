package com.lirium.nutrition.dto.response;

public record FoodPortionRecordResponseDTO(

        Long id,
        Long mealId,
        FoodSummaryDTO food,
        Integer grams

) {}