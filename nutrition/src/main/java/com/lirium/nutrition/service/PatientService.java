package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.PatientDetailsDTO;
import com.lirium.nutrition.dto.response.PatientSummaryDTO;

import java.util.List;

public interface PatientService {


    List<PatientSummaryDTO> searchPatients(
            String firstName,
            String lastName,
            String email,
            String dni
    );

    PatientDetailsDTO getPatientDetail(Long patientId);

    PatientDetailsDTO updatePatient(Long patientId, PatientUpdateRequestDTO request);

}