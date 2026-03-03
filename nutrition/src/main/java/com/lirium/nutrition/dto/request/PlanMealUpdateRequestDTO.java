package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record PlanMealUpdateRequestDTO(

        @Pattern(
                regexp = "BREAKFAST|LUNCH|DINNER|SNACK",
                message = "Invalid meal type"
        )
        String type,

        List<@NotNull Long> foodPortionIds

) {}