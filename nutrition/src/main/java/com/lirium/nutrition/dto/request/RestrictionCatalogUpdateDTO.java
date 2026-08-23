package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.RestrictionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating an existing restriction")
public record RestrictionCatalogUpdateDTO(

        @Schema(description = "Unique code for the restriction", example = "CELIAC")
        @NotBlank(message = "Code is required")
        @Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
        String code,

        @Schema(description = "Descriptive name of the restriction", example = "Celiac Disease")
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 80, message = "Name must be between 3 and 80 characters")
        String name,

        @Schema(description = "Detailed description", example = "Permanent intolerance to gluten")
        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @Schema(description = "Category of restriction", example = "PATHOLOGICAL")
        @NotNull(message = "Category is required")
        RestrictionCategory category
) {}