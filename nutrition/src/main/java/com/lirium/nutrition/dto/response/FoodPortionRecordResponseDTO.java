package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record FoodPortionRecordResponseDTO(
        Long id,
        String foodName,
        Double quantity,
        MeasureUnit unit,
        int calories,
        int protein,
        int carbs,
        int fat
) {}