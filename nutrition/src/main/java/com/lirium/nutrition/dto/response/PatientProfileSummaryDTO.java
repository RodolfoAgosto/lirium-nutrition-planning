package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;

public record PatientProfileSummaryDTO(

        Long id,
        Long userId,
        GoalType primaryGoal

) {}
