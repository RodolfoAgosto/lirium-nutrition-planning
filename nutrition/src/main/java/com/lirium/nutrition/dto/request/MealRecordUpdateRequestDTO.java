package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordUpdateRequestDTO(
        String notes
) {}