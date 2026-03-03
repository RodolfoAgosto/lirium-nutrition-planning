package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MealType;
import java.time.LocalDateTime;

public record MealRecordSummaryDTO(
        Long id,
        MealType type,
        LocalDateTime eatenAt,
        boolean overridden
) {}