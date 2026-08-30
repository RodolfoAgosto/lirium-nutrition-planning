package com.lirium.nutrition.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for updating a meal record (e.g. adding notes and overriding)")
public record MealRecordUpdateRequestDTO(
    @Schema(
            description = "Notes or reason for overriding the meal",
            example = "Replaced side dish with salad")
        @NotBlank(message = "Notes cannot be empty")
        String notes) {}
