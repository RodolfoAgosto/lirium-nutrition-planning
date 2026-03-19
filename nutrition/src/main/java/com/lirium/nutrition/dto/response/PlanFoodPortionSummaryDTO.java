package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;

public record PlanFoodPortionSummaryDTO(

        Long id,
        Long foodId,
        Double quantity,
        MeasureUnit uni

) {}