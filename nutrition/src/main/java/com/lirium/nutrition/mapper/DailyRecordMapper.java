package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.entity.FoodPortionRecord;
import com.lirium.nutrition.model.entity.MealRecord;

public class DailyRecordMapper {

    public static DailyRecordResponseDTO toResponse(DailyRecord record) {
        return new DailyRecordResponseDTO(
                record.getId(),
                record.getDate(),
                record.getMeals().stream()
                        .map(DailyRecordMapper::toMealResponse)
                        .toList()
        );
    }

    public static MealRecordResponseDTO toMealResponse(MealRecord meal) {
        return new MealRecordResponseDTO(
                meal.getId(),
                meal.getType(),
                meal.isOverridden(),
                meal.getNotes(),
                meal.getEatenAt(),
                meal.getFoodPortions().stream()
                        .map(DailyRecordMapper::toPortionResponse)
                        .toList()
        );
    }

    private static FoodPortionRecordResponseDTO toPortionResponse(FoodPortionRecord portion) {
        double grams = portion.grams();
        return new FoodPortionRecordResponseDTO(
                portion.getId(),
                portion.getFood().getName(),
                portion.getQuantity(),
                portion.getUnit(),
                (int)(portion.getFood().getCaloriesPer100g() * grams / 100),
                (int)(portion.getFood().getProteinPer100g()  * grams / 100),
                (int)(portion.getFood().getCarbsPer100g()    * grams / 100),
                (int)(portion.getFood().getFatPer100g()      * grams / 100)
        );
    }
}