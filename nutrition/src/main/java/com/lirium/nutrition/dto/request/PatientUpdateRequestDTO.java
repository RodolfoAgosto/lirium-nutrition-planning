package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.enums.Sex;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record PatientUpdateRequestDTO(
        String firstName,
        String lastName,
        String email,
        String dni,
        Sex sex,
        boolean enabled,
        LocalDate birthDate,
        Integer height,
        Integer weight,
        ActivityLevel activityLevel,
        GoalType goal,
        String medicalNotes,
        Set<RestrictionUpdateDTO> restrictions,
        List<PhysiologicalCondition> physiologicalConditions
) {}