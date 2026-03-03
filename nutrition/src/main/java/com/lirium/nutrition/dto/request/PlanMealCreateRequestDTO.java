package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record PlanMealCreateRequestDTO(

        @NotBlank(message = "Meal type is required")
        @Pattern(
                regexp = "BREAKFAST|LUNCH|DINNER|SNACK",
                message = "Invalid meal type"
        )
        String type,

        @NotNull(message = "Daily plan id is required")
        Long dailyPlanId,

        List<Long> foodPortionIds

) {}