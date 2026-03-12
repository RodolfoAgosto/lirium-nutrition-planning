package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PatientProfileResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PatientProfileMapper;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    public PatientProfile findByUserId(Long userId) {

        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient profile not found", userId)
                );
    }

    @Override
    @Transactional
    public PatientProfileResponseDTO update(PatientProfile patientProfile) {

        PatientProfile saved = patientProfileRepository.save(patientProfile);

        return patientProfileMapper.toResponse(saved);

    }
}