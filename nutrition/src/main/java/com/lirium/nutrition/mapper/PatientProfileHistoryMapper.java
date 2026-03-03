package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.PatientProfileHistory;

import java.util.stream.Collectors;

public class PatientProfileHistoryMapper {

    private PatientProfileHistoryMapper() {}

    public static PatientProfileHistoryResponseDTO toResponseDTO(PatientProfileHistory history) {
        return new PatientProfileHistoryResponseDTO(
                history.getId(),
                history.getPatientProfile().getId(),
                history.getVisitDate(),
                history.getWeight(),
                history.getHeight(),
                history.getMedicalNotes(),
                history.getRestrictions()
                        .stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()),
                history.getPhysiologicalConditions(),
                history.getPrimaryGoal()
        );
    }

    public static PatientProfileHistorySummaryDTO toSummaryDTO(PatientProfileHistory history) {
        return new PatientProfileHistorySummaryDTO(
                history.getId(),
                history.getPatientProfile().getId(),
                history.getVisitDate(),
                history.getWeight()
        );
    }
}