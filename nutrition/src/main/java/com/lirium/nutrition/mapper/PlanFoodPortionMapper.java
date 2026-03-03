package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.valueobject.Grams;

public class PlanFoodPortionMapper {

    private PlanFoodPortionMapper() {}

    /* === ENTITY -> RESPONSE === */

    public static PlanFoodPortionResponseDTO toResponse(PlanFoodPortion entity) {

        return new PlanFoodPortionResponseDTO(
                entity.getId(),
                entity.getMeal().getId(),
                entity.getFood().getId(),
                entity.getFood().getName(),
                entity.getGrams().value()
        );
    }

    public static PlanFoodPortionSummaryDTO toSummary(PlanFoodPortion entity) {

        return new PlanFoodPortionSummaryDTO(
                entity.getId(),
                entity.getFood().getId(),
                entity.getGrams().value()
        );
    }

    /* === CREATE DTO -> ENTITY === */

    public static PlanFoodPortion toEntity(
            PlanFoodPortionCreateRequestDTO dto,
            PlanMeal meal,
            Food food
    ) {

        Grams grams = new Grams(dto.grams());

        return PlanFoodPortion.of(meal, food, grams);
    }

    /* === UPDATE DTO -> ENTITY ==== */

    public static void updateEntity(
            PlanFoodPortion entity,
            PlanFoodPortionUpdateRequestDTO dto
    ) {

        if (dto.grams() != null) {
            entity.changeGrams(new Grams(dto.grams()));
        }
    }
}