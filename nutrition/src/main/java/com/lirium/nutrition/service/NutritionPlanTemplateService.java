package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;

import java.util.List;

public interface NutritionPlanTemplateService {

    List<NutritionPlanTemplateSummaryDTO> getAll();

    NutritionPlanTemplateResponseDTO getById(Long id);

    NutritionPlanTemplateResponseDTO create(NutritionPlanTemplateCreateRequestDTO dto);

    NutritionPlanTemplateResponseDTO update(
            Long id,
            NutritionPlanTemplateUpdateRequestDTO dto
    );

    void delete(Long id);

}
