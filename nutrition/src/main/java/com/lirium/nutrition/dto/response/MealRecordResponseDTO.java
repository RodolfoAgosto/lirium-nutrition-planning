package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MealType;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordResponseDTO(
        Long id,
        MealType type,
        LocalDateTime eatenAt,
        boolean overridden,
        String notes,
        List<FoodPortionRecordResponseDTO> foods
) {}