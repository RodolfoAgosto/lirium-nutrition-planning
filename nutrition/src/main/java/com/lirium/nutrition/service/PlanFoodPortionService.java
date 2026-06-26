package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.model.entity.PlanFoodPortion;

import java.util.List;

public interface PlanFoodPortionService {

    List<PlanFoodPortionResponseDTO> getByPlanMeal(Long planMealId);

    PlanFoodPortionResponseDTO getById(Long id);

    PlanFoodPortion findEntityById(Long id) ;

    PlanFoodPortionResponseDTO create(PlanFoodPortionCreateRequestDTO dto);

    void delete(Long id);

    PlanFoodPortionResponseDTO update(Long id, PlanFoodPortionUpdateFoodRequestDTO request);

}