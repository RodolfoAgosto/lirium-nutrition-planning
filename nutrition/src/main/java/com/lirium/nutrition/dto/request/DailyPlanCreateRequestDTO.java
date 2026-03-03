package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record DailyPlanCreateRequestDTO(

        @NotBlank(message = "Day is required")
        @Pattern(
                regexp = "MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY",
                message = "Invalid day"
        )
        String day,

        @NotNull(message = "Nutrition plan id is required")
        Long nutritionPlanId,

        List<Long> mealIds

) {}