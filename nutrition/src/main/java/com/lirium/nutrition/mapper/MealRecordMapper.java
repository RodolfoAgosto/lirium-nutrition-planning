package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.Grams;

import java.util.List;
import java.util.stream.Collectors;

public class MealRecordMapper {

    private MealRecordMapper() {}

    /* === ENTITY -> RESPONSE === */

    public static MealRecordResponseDTO toResponse(MealRecord entity) {

        List<FoodPortionRecordResponseDTO> foods =
                entity.getFoodPortions()
                        .stream()
                        .map(FoodPortionRecordMapper::toResponse)
                        .collect(Collectors.toList());

        return new MealRecordResponseDTO(
                entity.getId(),
                entity.getType(),
                entity.getEatenAt(),
                entity.isOverridden(),
                entity.getNotes(),
                foods
        );
    }

    public static MealRecordSummaryDTO toSummary(MealRecord entity) {
        return new MealRecordSummaryDTO(
                entity.getId(),
                entity.getType(),
                entity.getEatenAt(),
                entity.isOverridden()
        );
    }

    /* === CREATE DTO -> ENTITY === */

    public static MealRecord toEntity(
            MealRecordCreateRequestDTO dto,
            List<Food> foodsFromDB
    ) {

        MealType type = MealType.valueOf(dto.type().toUpperCase());

        MealRecord meal = MealRecord.of(type, dto.eatenAt());

        if (dto.notes() != null && !dto.notes().isBlank()) {
            meal.updateNotes(dto.notes());
        }

        if (dto.foods() != null) {
            for (int i = 0; i < dto.foods().size(); i++) {

                FoodPortionCreateDTO portionDTO = dto.foods().get(i);
                Food food = foodsFromDB.get(i); // mismo orden

                meal.addFoodPortion(food, new Grams(portionDTO.grams()));
            }
        }

        return meal;
    }

}