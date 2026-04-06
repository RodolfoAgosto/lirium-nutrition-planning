package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequest;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.model.entity.NutritionPlan;

import java.util.List;
import java.util.Optional;

public interface NutritionPlanService {

    NutritionPlanDetailDTO complete(Long id, CompleteNutritionPlanRequest request);

    NutritionPlan createFromTemplate(Long patientId, Long templateId);

    NutritionPlanDetailDTO activatePlan(Long planId);

    NutritionPlanDetailDTO findById(Long id);

    List<NutritionPlanSummaryDTO> findByPatient(Long patientId);

    Optional<NutritionPlan> findActivePlan(Long patientId);

}