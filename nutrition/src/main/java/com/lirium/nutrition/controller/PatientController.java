package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        return ResponseEntity.ok(patientService.searchPatients(firstName, lastName, email, dni));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDetailsDTO> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDetailsDTO> updateProfile(
            @PathVariable Long id,
            @RequestBody PatientUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id,requestDTO));
    }

}