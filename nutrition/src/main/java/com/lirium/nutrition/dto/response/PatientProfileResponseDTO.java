package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PatientProfileResponseDTO(

        Long id,
        Long userId,
        Sex sex,
        ActivityLevel activityLevel,
        BigDecimal weight,
        Integer height,
        String medicalNotes,
        Set<String> restrictions,
        List<PhysiologicalCondition> physiologicalConditions,
        GoalType primaryGoal

) {}