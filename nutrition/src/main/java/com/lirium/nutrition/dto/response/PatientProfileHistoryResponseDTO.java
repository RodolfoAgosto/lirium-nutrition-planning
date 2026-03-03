package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record PatientProfileHistoryResponseDTO(

        Long id,
        Long patientProfileId,
        LocalDate visitDate,
        BigDecimal weight,
        Integer height,
        String medicalNotes,
        Set<String> restrictions,
        Set<PhysiologicalCondition> physiologicalConditions,
        GoalType primaryGoal

) {}