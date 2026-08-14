package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.service.NutritionPlanGenerator;
import com.lirium.nutrition.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition-plans")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Nutrition Plans", description = "Endpoints for generating, retrieving, and managing nutritional plans.")
public class NutritionPlanController {

    private final NutritionPlanGenerator nutritionPlanGenerator;
    private final NutritionPlanService nutritionPlanService;

    @Operation(
            summary = "Generate a nutrition plan for a patient",
            description = "Executes the calculation engine to create a tailored nutrition plan based on the patient's metrics and target goals."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Nutrition plan successfully generated",
                    headers = @Header(name = "Location", description = "URI of the newly generated nutrition plan", schema = @Schema(type = "string")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NutritionPlanDetailDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid patient ID or parameters out of bounds", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access / Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges to generate plan for this patient", content = @Content),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity: Missing required physical metrics or goals for patient", content = @Content)
    })
    @PreAuthorize("hasAnyRole('NUTRITIONIST', 'ADMIN')")
    @PostMapping("/generate/{patientId}")
    public ResponseEntity<NutritionPlanDetailDTO> generate(
                @PathVariable("patientId")
                @NotNull(message = "Patient ID is required")
                @Positive(message = "Patient ID must be a positive number")
                        Long patientId
    ) {

        log.info("Generating nutrition plan for patientId={}", patientId);
        NutritionPlanDetailDTO dto = nutritionPlanGenerator.generate(patientId);
        log.info("Nutrition plan generated successfully for patientId={}", patientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);

    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<NutritionPlanDetailDTO> complete(
            @PathVariable Long id,
            @RequestBody CompleteNutritionPlanRequestDTO request) {

        log.info("Completing nutrition plan id={}", id);
        log.debug("Complete plan payload={}", request.toString());
        NutritionPlanDetailDTO response = nutritionPlanService.complete(id, request);
        log.info("Nutrition plan completed id={}", id);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {

        log.info("Activating nutrition plan id={}", id);
        nutritionPlanService.activatePlan(id);
        log.info("Nutrition plan activated id={}", id);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/generate-from-template/{patientId}/{templateId}")
    public NutritionPlanDetailDTO generateFromTemplate(
            @PathVariable Long patientId,
            @PathVariable Long templateId) {

        log.info("Generating nutrition plan from template templateId={} for patientId={}", templateId, patientId);
        NutritionPlanDetailDTO response = nutritionPlanGenerator.generateFromTemplate(patientId, templateId);
        log.info("Nutrition plan generated from template templateId={} for patientId={}", templateId, patientId);
        return response;

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @nutritionPlanService.belongsToPatient(#id, authentication.principal.id)")
    public ResponseEntity<NutritionPlanDetailDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(nutritionPlanService.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    //In the case where the PATIENT accesses by plan ID (not by PatientId)
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or #patientId == authentication.principal.id")
    public ResponseEntity<List<NutritionPlanSummaryDTO>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(nutritionPlanService.findByPatient(patientId));
    }

}