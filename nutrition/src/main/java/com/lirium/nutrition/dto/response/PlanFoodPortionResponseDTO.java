package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;

public record PlanFoodPortionResponseDTO(

        Long id,
        Long mealId,
        Long foodId,
        String foodName,
        Double quantity,
        MeasureUnit unit

) {}