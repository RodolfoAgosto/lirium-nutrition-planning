package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionSummaryDTO;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;

public class PlanFoodPortionMapper {

    private PlanFoodPortionMapper() {}

    /* === ENTITY -> RESPONSE === */

    public static PlanFoodPortionResponseDTO toResponse(PlanFoodPortion entity) {

        return new PlanFoodPortionResponseDTO(
                entity.getId(),
                entity.getMeal().getId(),
                entity.getFood().getId(),
                entity.getFood().getName(),
                entity.getQuantity(),
                entity.getMeasureUnit()
        );
    }

    public static PlanFoodPortionSummaryDTO toSummary(PlanFoodPortion entity) {

        return new PlanFoodPortionSummaryDTO(
                entity.getId(),
                entity.getFood().getId(),
                entity.getQuantity(),
                entity.getMeasureUnit()
                );
    }

    /* === CREATE DTO -> ENTITY === */

    public static PlanFoodPortion toEntity(
            PlanFoodPortionCreateRequestDTO dto,
            PlanMeal meal,
            Food food
    ) {
        return PlanFoodPortion.of(meal,food, dto.quantity(), dto.unit());
    }

    /* === UPDATE DTO -> ENTITY ==== */

    public static PlanFoodPortion toEntity(
            FoodPortionAddRequestDTO dto,
            PlanMeal meal,
            Food food
    ) {

        return PlanFoodPortion.of(meal,food, dto.quantity(), dto.unit());

    }
}