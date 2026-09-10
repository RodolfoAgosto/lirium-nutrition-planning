package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.PatientDetailDTO;
import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import java.util.List;

public interface PatientService {

  List<PatientSummaryDTO> searchPatients(
      String firstName, String lastName, String email, String dni);

  PatientDetailDTO getPatientDetail(Long patientId);

  PatientDetailDTO updatePatient(Long patientId, PatientUpdateRequestDTO request);
}
