package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PatientProfileUpdateRequestDTO(

        Sex sex,
        ActivityLevel activityLevel,

        @Min(20)
        @Max(400)
        Integer weight,

        @Min(80)
        @Max(250)
        Integer height,

        @Size(max = 2000)
        String medicalNotes,

        Set<Long> restrictionIds,
        List<PhysiologicalCondition> physiologicalConditions,
        GoalType primaryGoal

) {}
