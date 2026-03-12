package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class PatientProfileMapper {

    private PatientProfileMapper() {}

    // ==== CREATE ===
    public static PatientProfile toEntity(
            PatientProfileCreateRequestDTO dto,
            User user,
            Set<Restriction> restrictions
    ) {

        PatientProfile profile = new PatientProfile(user);

        profile.update(
                dto.sex(),
                dto.activityLevel(),
                Weight.of(dto.weight()),
                Height.of(dto.height()),
                dto.medicalNotes(),
                restrictions,
                dto.physiologicalConditions(),
                dto.primaryGoal()
        );

        return profile;
    }

    // === UPDATE ===
    public static void updateEntity(
            PatientProfile entity,
            PatientProfileUpdateRequestDTO dto,
            Set<Restriction> restrictions
    ) {

        entity.update(
                dto.sex(),
                dto.activityLevel(),
                Weight.of(dto.weight()),
                Height.of(dto.height()),
                dto.medicalNotes(),
                restrictions,
                dto.physiologicalConditions(),
                dto.primaryGoal()
        );
    }

    // === RESPONSE ===
    public static PatientProfileResponseDTO toResponse(PatientProfile entity) {

        Set<String> restrictionNames =
                entity.getRestrictions().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet());

        return new PatientProfileResponseDTO(
                entity.getId(),
                entity.getUser().getId(),
                entity.getSex(),
                entity.getActivityLevel(),
                entity.getWeight().grams(),
                entity.getHeight().cm(),
                entity.getMedicalNotes(),
                restrictionNames,
                entity.getPhysiologicalConditions(),
                entity.getPrimaryGoal()
        );
    }

    // === SUMMARY ===
    public static PatientProfileSummaryDTO toSummary(PatientProfile entity) {

        return new PatientProfileSummaryDTO(
                entity.getId(),
                entity.getUser().getId(),
                entity.getPrimaryGoal()
        );
    }
}