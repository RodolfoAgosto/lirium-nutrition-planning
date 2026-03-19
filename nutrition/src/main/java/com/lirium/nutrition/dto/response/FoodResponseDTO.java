package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;

import java.util.HashSet;
import java.util.Set;

public record FoodResponseDTO(

        Long id,
        String name,
        Integer caloriesPer100g,
        Integer proteinPer100g,
        Integer carbsPer100g,
        Integer fatPer100g,
        FoodCategory foodCategory,
        Set<MealType> suitableFor,
        Set<FoodTag> tags

) {}