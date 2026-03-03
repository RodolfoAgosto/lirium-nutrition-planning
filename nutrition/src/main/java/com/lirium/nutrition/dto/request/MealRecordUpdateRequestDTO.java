package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordUpdateRequestDTO(
        LocalDateTime eatenAt,
        @Size(max = 500) String notes,
        Boolean overridden,
        List<FoodPortionCreateDTO> foods
) {}