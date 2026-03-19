package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.valueobject.Grams;

public class FoodPortionRecordMapper {

    private FoodPortionRecordMapper() {}

    /* === ENTITY -> RESPONSE ==== */

    public static FoodPortionRecordResponseDTO toResponse(FoodPortionRecord entity) {
        return new FoodPortionRecordResponseDTO(
                entity.getId(),
                entity.getMeal().getId(),
                FoodMapper.toSummary(entity.getFood()),
                entity.getQuantity(),
                entity.getUnit()
        );
    }

    public static FoodPortionRecordSummaryDTO toSummary(FoodPortionRecord entity) {

        return new FoodPortionRecordSummaryDTO(
                entity.getId(),
                entity.getFood().getId(),
                entity.getQuantity(),
                entity.getUnit()
        );
    }

    /* ==== CREATE DTO -> ENTITY === */

    public static FoodPortionRecord toEntity(
            FoodPortionRecordCreateRequestDTO dto,
            MealRecord meal,
            Food food
    ) {

        return FoodPortionRecord.of(meal, food, dto.quantity(), dto.unit());
    }

}