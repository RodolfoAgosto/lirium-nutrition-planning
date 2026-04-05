package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.AddFoodPortionRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;

import java.util.List;

public interface DailyRecordService {
    DailyRecordResponseDTO getOrCreateToday(Long patientId);
    DailyRecordResponseDTO getById(Long id);
    List<DailyRecordResponseDTO> getByPatient(Long patientId);
    MealRecordResponseDTO updateMeal(Long mealRecordId, MealRecordUpdateRequestDTO request);
    MealRecordResponseDTO addPortion(Long mealRecordId, AddFoodPortionRequestDTO request);
    void removePortion(Long dailyRecordId, Long mealRecordId, Long portionId);
}