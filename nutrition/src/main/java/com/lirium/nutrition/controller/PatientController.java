package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.PatientDetailsDTO;
import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import com.lirium.nutrition.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(
    name = "Patients",
    description = "Endpoints for searching, retrieving, and updating patient profiles")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

  private final PatientService patientService;

  @Operation(
      operationId = "searchPatients",
      summary = "Search patients by filters",
      description =
          "Retrieves a list of patients filtered by first name, last name, email, or DNI. Restricted to ADMIN and NUTRITIONIST roles.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patients retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(schema = @Schema(implementation = PatientSummaryDTO.class))))
      })
  @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST')")
  @GetMapping("/search")
  public ResponseEntity<List<PatientSummaryDTO>> findPatients(
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String dni) {

    log.info(
        "Searching patients with filters firstName={}, lastName={}, email={}, dni={}",
        firstName,
        lastName,
        email,
        dni);
    List<PatientSummaryDTO> response =
        patientService.searchPatients(firstName, lastName, email, dni);
    log.info("Patient search completed resultsCount={}", response.size());
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "getPatientById",
      summary = "Get patient by ID",
      description =
          "Retrieves detailed profile of a specific patient. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient profile retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PatientDetailsDTO.class)))
      })
  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#id, authentication)")
  public ResponseEntity<PatientDetailsDTO> getPatient(@PathVariable Long id) {
    return ResponseEntity.ok(patientService.getPatientDetail(id));
  }

  @Operation(
      operationId = "updatePatientProfile",
      summary = "Update patient profile",
      description =
          "Updates personal details of an existing patient. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient profile updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PatientDetailsDTO.class)))
      })
  @PutMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#id, authentication)")
  public ResponseEntity<PatientDetailsDTO> updateProfile(
      @PathVariable("id") @Positive(message = "The ID must be a positive integer.") Long id,
      @Valid @RequestBody PatientUpdateRequestDTO requestDTO) {

    log.info("Updating patient profile id={}", id);
    log.debug("Patient update payload={}", requestDTO.toString());
    PatientDetailsDTO response = patientService.updatePatient(id, requestDTO);
    log.info("Patient profile updated successfully id={}", id);
    return ResponseEntity.ok(response);
  }
}
