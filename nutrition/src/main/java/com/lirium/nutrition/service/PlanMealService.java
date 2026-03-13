package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import java.util.List;

public interface PlanMealService {

    PlanMealResponseDTO getById(Long id);

    List<PlanMealSummaryDTO> getByPlanDay(Long planDayId);

    PlanMealResponseDTO create(PlanMealCreateRequestDTO dto);

    void delete(Long id);

}