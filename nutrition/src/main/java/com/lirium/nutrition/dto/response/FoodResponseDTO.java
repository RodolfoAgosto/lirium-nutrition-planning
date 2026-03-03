package com.lirium.nutrition.dto.response;

import java.util.Set;

public record FoodResponseDTO(

        Long id,
        String name,
        Integer caloriesPer100g,
        Integer proteinPer100g,
        Integer carbsPer100g,
        Integer fatPer100g,
        Set<String> tags

) {}