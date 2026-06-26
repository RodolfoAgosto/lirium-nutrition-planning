package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.PlanFoodPortion;

import java.util.List;

public interface PlanMealService {

    PlanMealResponseDTO getById(Long id);

    List<PlanMealSummaryDTO> getByPlanDay(Long planDayId);

    PlanMealResponseDTO create(PlanMealCreateRequestDTO dto);

    void delete(Long id);

    PlanMealResponseDTO addPortion(Long mealId, FoodPortionAddRequestDTO dto);

    PlanMealResponseDTO removePortion(Long mealId, Long portionId);

    PlanMealResponseDTO updatePortion(Long mealId, Long portionId, PlanFoodPortionUpdateFoodRequestDTO dto);

}