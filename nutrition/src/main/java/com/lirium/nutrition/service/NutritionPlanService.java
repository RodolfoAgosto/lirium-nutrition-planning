package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.NutritionPlan;

import java.util.List;
import java.util.Optional;

public interface NutritionPlanService {

    NutritionPlan createFromTemplate(Long patientId, Long templateId);

    NutritionPlan activatePlan(Long planId);

    NutritionPlan findById(Long planId);

    List<NutritionPlan> findByPatient(Long patientId);

    Optional<NutritionPlan> findActivePlan(Long patientId);

}