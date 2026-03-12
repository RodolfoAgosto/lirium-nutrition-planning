package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RestrictionUpdateDTO(
        @NotBlank String code
) {}
