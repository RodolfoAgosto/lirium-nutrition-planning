package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.GoalType;
import java.time.LocalDate;

public record NutritionPlanResponseDTO(

        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        GoalType targetGoal,
        int dailyCalories,
        int proteinGrams,
        int carbGrams,
        int fatGrams,
        int daysCount

) {}
