package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/search")
    public ResponseEntity<List<PatientSummaryDTO>> findPatients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dni) {

        log.info("Searching patients with filters firstName={}, lastName={}, email={}, dni={}", firstName, lastName, email, dni);
        List<PatientSummaryDTO> response = patientService.searchPatients(firstName, lastName, email, dni);
        log.info("Patient search completed resultsCount={}", response.size());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDetailsDTO> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDetailsDTO> updateProfile(
            @PathVariable Long id,
            @RequestBody PatientUpdateRequestDTO requestDTO) {

        log.info("Updating patient profile id={}", id);
        if (log.isDebugEnabled()) {
            log.debug("Patient update payload={}", requestDTO.toString());
        }
        PatientDetailsDTO response = patientService.updatePatient(id, requestDTO);
        log.info("Patient profile updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

}