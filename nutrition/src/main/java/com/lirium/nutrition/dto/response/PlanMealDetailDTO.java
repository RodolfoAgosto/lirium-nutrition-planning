package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MealType;

import java.util.List;

public record PlanMealDetailDTO(
        MealType type,
        List<PlanFoodPortionDetailDTO> portions
) {}
