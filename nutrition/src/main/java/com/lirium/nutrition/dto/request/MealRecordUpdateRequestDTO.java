package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordUpdateRequestDTO(
        @NotBlank String notes
) {}