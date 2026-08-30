package com.lirium.nutrition.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description =
        "Payload to complete an active nutrition plan and record its clinical closing summary")
public record CompleteNutritionPlanRequestDTO(
    @Schema(
            description =
                "Title or label to identify this completed milestone in the patient's medical history.",
            example = "Phase 1: Fat Loss Deficit Jan-Mar")
        @NotBlank(message = "Closure name is required")
        @Size(max = 100, message = "Closure name cannot exceed 100 characters")
        String name,
    @Schema(
            description =
                "Clinical notes, conclusions, or summary of results achieved during the duration of the plan.",
            example =
                "Patient achieved 85% adherence. Reduced 3.2 kg of fat mass while maintaining muscle mass.")
        @NotBlank(message = "Closure description is required")
        @Size(max = 1000, message = "Closure description cannot exceed 1000 characters")
        String description) {}
