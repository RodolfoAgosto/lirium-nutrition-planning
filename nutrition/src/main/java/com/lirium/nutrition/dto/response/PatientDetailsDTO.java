package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;

import java.time.LocalDate;
import java.util.Set;

public record PatientDetailsDTO(

    Long patientId,
    String firstName,
    String lastName,
    String email,
    String dni,
    Sex sex,
    boolean enabled,
    LocalDate birthDate,
    Height height,
    Weight weight,
    ActivityLevel activityLevel,
    GoalType goal,
    String medicalNotes,
    Set<RestrictionSummaryDTO> restrictions,
    Set<PhysiologicalCondition> physiologicalConditions){

}