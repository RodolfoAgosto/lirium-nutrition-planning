package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;

public record FoodPortionRecordSummaryDTO(

        Long id,
        Long foodId,
        Double quantity,
        MeasureUnit unit

) {}