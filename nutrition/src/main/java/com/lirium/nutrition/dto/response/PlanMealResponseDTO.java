package com.lirium.nutrition.dto.response;

import java.util.List;

public record PlanMealResponseDTO(

        Long id,
        String type,
        Long dailyPlanId,
        List<PlanFoodPortionSummaryDTO> foods

) {}