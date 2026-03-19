package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record FoodPortionRecordResponseDTO(

        Long id,
        Long mealId,
        FoodSummaryDTO food,
        Double quantity,
        MeasureUnit unit

) {}