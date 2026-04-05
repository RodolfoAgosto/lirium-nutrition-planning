package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.MeasureUnit;

public record AddFoodPortionRequestDTO(
        Long foodId,
        Double quantity,
        MeasureUnit unit
) {}