package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.RestrictionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RestrictionCreateRequestDTO(

        @NotBlank(message = "Code is required")
        @Size(min = 2, max = 30, message = "Code must be between 2 and 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Schema(description = "Category of the restriction", example = "PATHOLOGICAL")
        @NotNull(message = "Category is required")
        RestrictionCategory category
) {}