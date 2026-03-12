package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.PatientProfileResponseDTO;
import com.lirium.nutrition.model.entity.PatientProfile;

public interface PatientProfileService {

    public PatientProfile findByUserId(Long patientId);

    PatientProfileResponseDTO update(PatientProfile patientProfile);

}
