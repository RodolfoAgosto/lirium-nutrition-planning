package com.lirium.nutrition.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RestrictionUpdateDTO(

        @Schema(example = "GLUTEN_FREE")
        @NotBlank(message = "Restriction code is required")
        String code
) {}