package com.lirium.nutrition.dto.request;

import java.util.List;

public record DailyPlanUpdateRequestDTO(
        List<Long> mealIds
) {}