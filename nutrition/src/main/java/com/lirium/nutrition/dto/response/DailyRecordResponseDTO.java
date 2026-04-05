package com.lirium.nutrition.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyRecordResponseDTO(
        Long id,
        LocalDate date,
        List<MealRecordResponseDTO> meals
) {}
