package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record DailyAdherenceDTO(
        LocalDate date,
        int expectedMeals,
        int recordedMeals,
        boolean hasRecord
) {}
