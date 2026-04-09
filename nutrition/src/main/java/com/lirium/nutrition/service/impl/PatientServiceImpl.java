package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.RestrictionMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.*;
import com.lirium.nutrition.service.PatientProfileService;
import com.lirium.nutrition.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {

    private final PatientProfileService patientProfileService;
    private final PatientProfileRepository patientProfileRepository;
    private final RestrictionRepository restrictionRepository;
    private final RestrictionMapper restrictionMapper;

    @Override
    public List<PatientSummaryDTO> searchPatients(
            String firstName,
            String lastName,
            String email,
            String dni) {

        return patientProfileRepository.searchPatients(firstName, lastName, email, dni);
    }

    @Override
    public PatientDetailsDTO getPatientDetail(Long patientId) {

        log.info("Fetching patient detail patientId={}", patientId);

        PatientProfile profile = patientProfileRepository
                .findByIdWithUser(patientId)
                .orElseThrow(() -> {
                    log.warn("Patient not found id={}", patientId);
                    return new ResourceNotFoundException("Patient not found", patientId);
                });

        User user = profile.getUser();
        // Restriction List
        Set<RestrictionSummaryDTO> restrictions = profile.getRestrictions()
                .stream()
                .map(restrictionMapper::toSummaryDTO).collect(Collectors.toSet());

        // Physiological Condition List
        Set<PhysiologicalCondition> physiologicalConditions = profile.getPhysiologicalConditions();

        log.info("Patient detail fetched successfully patientId={}", patientId);

        return new PatientDetailsDTO(
                profile.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDni(),
                profile.getSex(),
                user.isEnabled(),
                user.getBirthDate(),
                profile.getHeight(),
                profile.getWeight(),
                profile.getActivityLevel(),
                profile.getPrimaryGoal(),
                profile.getMedicalNotes(),
                restrictions,
                physiologicalConditions);
    };

    @Override
    @Transactional
    public PatientDetailsDTO updatePatient(Long patientId, PatientUpdateRequestDTO request) {

        Objects.requireNonNull(request, "PatientUpdateRequestDTO must not be null");

        log.info("Updating patient patientId={}", patientId);

        PatientProfile profile = patientProfileService.findByUserId(patientId);
        Set<Restriction> restrictions = resolveRestrictions(request.restrictions());

        List<PhysiologicalCondition> physiologicalConditions =
                request.physiologicalConditions();

        User user = profile.getUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setBirthDate(request.birthDate());
        user.setEnabled(request.enabled());
        user.setEmailValidated(false);
        user.setDni(request.dni());

        profile.update(
                request.sex(),
                request.activityLevel(),
                Weight.of(request.weight()),
                Height.of(request.height()),
                request.medicalNotes(),
                restrictions,
                physiologicalConditions,
                request.goal()
        );

        log.info("Patient updated successfully patientId={}", patientId);

        return new PatientDetailsDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDni(),
                profile.getSex(),
                user.isEnabled(),
                user.getBirthDate(),
                profile.getHeight(),
                profile.getWeight(),
                profile.getActivityLevel(),
                profile.getPrimaryGoal(),
                profile.getMedicalNotes(),
                restrictionMapper.toDTOSet(profile.getRestrictions()),
                profile.getPhysiologicalConditions()
        );
    }

    private Set<Restriction> resolveRestrictions(Set<RestrictionUpdateDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Set.of();

        Set<String> codes = dtos.stream()
                .map(RestrictionUpdateDTO::code)
                .collect(Collectors.toSet());

        Set<Restriction> restrictions = restrictionRepository.findByCodes(codes);

        if (restrictions.size() != codes.size()) {
            Set<String> foundCodes = restrictions.stream()
                    .map(Restriction::getCode)
                    .collect(Collectors.toSet());
            codes.removeAll(foundCodes);
            throw new ResourceNotFoundException("Restrictions not found", codes.toString());
        }

        return restrictions;
    }
}