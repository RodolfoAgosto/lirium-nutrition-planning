package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.FoodPortionCreateDTO;
import com.lirium.nutrition.dto.request.MealRecordCreateRequestDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordSummaryDTO;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.MealRecord;
import com.lirium.nutrition.model.enums.MealType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MealRecordMapper {

  private MealRecordMapper() {}

  /* === ENTITY -> RESPONSE === */

  public static MealRecordResponseDTO toResponse(MealRecord entity) {

    List<FoodPortionRecordResponseDTO> foods =
        entity.getFoodPortions().stream()
            .map(FoodPortionRecordMapper::toResponse)
            .collect(Collectors.toList());

    return new MealRecordResponseDTO(
        entity.getId(),
        entity.getType(),
        entity.isOverridden(),
        entity.getNotes(),
        entity.getEatenAt(),
        foods);
  }

  public static MealRecordSummaryDTO toSummary(MealRecord entity) {
    return new MealRecordSummaryDTO(
        entity.getId(), entity.getType(), entity.getEatenAt(), entity.isOverridden());
  }

  /* === CREATE DTO -> ENTITY === */

  public static MealRecord toEntity(
      MealRecordCreateRequestDTO dto, List<Food> foodsFromDB, DailyRecord dailyRecord) {

    MealType type = MealType.valueOf(dto.type().toUpperCase());

    MealRecord meal = MealRecord.of(type, dto.eatenAt(), dailyRecord);

    if (dto.notes() != null && !dto.notes().isBlank()) {
      meal.updateNotes(dto.notes());
    }

    if (dto.foods() != null && !dto.foods().isEmpty()) {
      Map<Long, Food> foodMap = foodsFromDB.stream().collect(Collectors.toMap(Food::getId, f -> f));

      for (FoodPortionCreateDTO portionDTO : dto.foods()) {
        Food food = foodMap.get(portionDTO.foodId());
        if (food == null) {
          throw new IllegalArgumentException("Food not found for ID: " + portionDTO.foodId());
        }
        meal.addFoodPortion(food, portionDTO.quantity(), portionDTO.unit());
      }
    }

    return meal;
  }
}
