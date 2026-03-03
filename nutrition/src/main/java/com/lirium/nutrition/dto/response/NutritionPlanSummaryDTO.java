package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;
import java.time.LocalDate;

public record NutritionPlanSummaryDTO(

        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        GoalType targetGoal,
        int dailyCalories

) {}