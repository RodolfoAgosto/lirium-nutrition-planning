package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateQuantityRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;

import java.util.List;

public interface PlanMealService {

    PlanMealResponseDTO getById(Long id);

    List<PlanMealSummaryDTO> getByPlanDay(Long planDayId);

    PlanMealResponseDTO create(PlanMealCreateRequestDTO dto);

    void delete(Long id);

    PlanMealResponseDTO addPortion(Long mealId, FoodPortionAddRequestDTO dto);

    PlanMealResponseDTO removePortion(Long mealId, Long portionId);

    PlanMealResponseDTO updateQuantity(Long mealId, Long portionId, PlanFoodPortionUpdateQuantityRequestDTO dto);

}