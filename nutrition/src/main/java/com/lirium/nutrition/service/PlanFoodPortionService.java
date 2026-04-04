package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.dto.request.*;

import java.util.List;

public interface PlanFoodPortionService {

    List<PlanFoodPortionResponseDTO> getByPlanMeal(Long planMealId);

    PlanFoodPortionResponseDTO getById(Long id);

    PlanFoodPortionResponseDTO create(PlanFoodPortionCreateRequestDTO dto);

    void delete(Long id);

    PlanFoodPortionResponseDTO update(Long id, UpdatePlanFoodPortionRequestDTO request);

}