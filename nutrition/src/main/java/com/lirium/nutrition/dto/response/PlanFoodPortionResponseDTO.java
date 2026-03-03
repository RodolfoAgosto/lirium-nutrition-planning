package com.lirium.nutrition.dto.response;

public record PlanFoodPortionResponseDTO(

        Long id,
        Long mealId,
        Long foodId,
        String foodName,
        Integer grams

) {}