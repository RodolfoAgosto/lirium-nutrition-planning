package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanResponseDTO;

public interface NutritionPlanGenerator {

    NutritionPlanDetailDTO generate(Long userId);

    NutritionPlanDetailDTO generateFromTemplate(Long userId, Long templateId);

}
