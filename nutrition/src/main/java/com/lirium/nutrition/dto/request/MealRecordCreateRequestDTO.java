package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordCreateRequestDTO(

        @NotBlank
        @Pattern(
                regexp = "BREAKFAST|LUNCH|DINNER|SNACK|MID_MORNING",
                message = "Invalid meal type"
        )
        String type,

        @NotNull
        LocalDateTime eatenAt,

        @Size(max = 500)
        String notes,

        List<FoodPortionCreateDTO> foods
) {}