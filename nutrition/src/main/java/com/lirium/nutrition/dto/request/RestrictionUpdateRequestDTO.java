package com.lirium.nutrition.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description =
        "It is used to update an existing restriction within the context of a patient/user by sending only its code.")
public record RestrictionUpdateRequestDTO(
    @Schema(example = "GLUTEN_FREE") @NotBlank(message = "Restriction code is required")
        String code) {}
