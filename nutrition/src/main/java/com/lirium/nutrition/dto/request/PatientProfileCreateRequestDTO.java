package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.enums.Sex;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public record PatientProfileCreateRequestDTO(

        @NotNull
        Long userId,

        @NotNull
        Sex sex,

        @NotNull
        ActivityLevel activityLevel,

        @DecimalMin("20.0")
        @DecimalMax("300000.0")
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