package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;

import java.util.Set;
import java.util.stream.Collectors;

public class FoodMapper {

    private FoodMapper() {}

    /* === ENTITY -> RESPONSE === */

    public static FoodResponseDTO toResponse(Food entity) {

        Set<String> tags = entity.getFoodTags()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new FoodResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCaloriesPer100g(),
                entity.getProteinPer100g(),
                entity.getCarbsPer100g(),
                entity.getFatPer100g(),
                entity.getCategory(),
                entity.getSuitableFor(),
                entity.getFoodTags()
        );

    }

    public static FoodSummaryDTO toSummary(Food entity) {
        return new FoodSummaryDTO(
                entity.getId(),
                entity.getName()
        );
    }

    /* ========= CREATE DTO -> ENTITY ========= */

    public static Food toEntity(FoodCreateRequestDTO dto) {

        Food food = Food.of(
                dto.name(),
                dto.caloriesPer100g(),
                dto.proteinPer100g(),
                dto.carbsPer100g(),
                dto.fatPer100g(),
                dto.category(),
                dto.suitableFor()
        );

        if (dto.tags() != null) {
            dto.tags().forEach(tag ->
                    food.addTag(FoodTag.valueOf(tag.toUpperCase()))
            );
        }

        return food;
    }

    /* ========= UPDATE DTO -> ENTITY ========= */

    public static void updateEntity(
            Food entity,
            FoodUpdateRequestDTO dto
    ) {

        if (dto.name() != null)
            entity.changeName(dto.name());   // método de dominio recomendado

        if (dto.caloriesPer100g() != null)
            entity.changeCalories(dto.caloriesPer100g());

        if (dto.proteinPer100g() != null)
            entity.changeProtein(dto.proteinPer100g());

        if (dto.carbsPer100g() != null)
            entity.changeCarbs(dto.carbsPer100g());

        if (dto.fatPer100g() != null)
            entity.changeFat(dto.fatPer100g());

        if (dto.tags() != null) {
            entity.clearTags();
            dto.tags().forEach(tag ->
                    entity.addTag(FoodTag.valueOf(tag.toUpperCase()))
            );
        }
    }
}