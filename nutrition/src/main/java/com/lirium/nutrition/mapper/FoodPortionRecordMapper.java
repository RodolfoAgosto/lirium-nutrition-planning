package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.FoodPortionRecordCreateRequestDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordResponseDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordSummaryDTO;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.FoodPortionRecord;
import com.lirium.nutrition.model.entity.MealRecord;

public class FoodPortionRecordMapper {

    private FoodPortionRecordMapper() {}

    /* === ENTITY -> RESPONSE ==== */

    public static FoodPortionRecordResponseDTO toResponse(FoodPortionRecord entity) {
        return new FoodPortionRecordResponseDTO(
                entity.getId(),
                entity.getFood().getName(),
                entity.getQuantity(),
                entity.getFood().getDefaultUnit(),
                entity.calories().amount(),
                entity.protein().grams(),
                entity.carbs().amount(),
                entity.fat().amount()
        );
    }

    public static FoodPortionRecordSummaryDTO toSummary(FoodPortionRecord entity) {

        return new FoodPortionRecordSummaryDTO(
                entity.getId(),
                entity.getFood().getId(),
                entity.getQuantity(),
                entity.getMeasureUnit()
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