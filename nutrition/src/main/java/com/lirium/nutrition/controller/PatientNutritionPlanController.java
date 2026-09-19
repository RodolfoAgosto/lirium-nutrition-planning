package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.exception.ApiError;
import com.lirium.nutrition.infrastructure.config.CommonAuthResponses;
import com.lirium.nutrition.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for a patient's nutrition plans, nested under the owning patient's URL as the resource
 * hierarchy dictates (a patient HAS nutrition plans). Operations addressed by a plan's own id (get
 * by id, generate, complete, activate) live in {@link NutritionPlanController} at {@code
 * /api/nutrition-plans/{id}...} instead.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients/{patientId}/nutrition-plans")
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Nutrition Plans",
    description = "Endpoints for retrieving a patient's nutritional plans.")
public class PatientNutritionPlanController {

  private final NutritionPlanService nutritionPlanService;

  @CommonAuthResponses
  @Operation(
      operationId = "getNutritionPlansByPatient",
      summary = "Get nutrition plans by patient ID",
      description =
          "Retrieves all nutrition plans associated with a specific patient. Accessible by ADMIN, NUTRITIONIST, or the target patient.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Nutrition plans retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = NutritionPlanSummaryDTO.class))))
      })
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or #patientId == authentication.principal.id")
  public ResponseEntity<List<NutritionPlanSummaryDTO>> findByPatient(@PathVariable Long patientId) {
    return ResponseEntity.ok(nutritionPlanService.findByPatient(patientId));
  }

  @CommonAuthResponses
  @Operation(
      operationId = "getActiveNutritionPlanByPatient",
      summary = "Get current active nutrition plan for a patient",
      description =
          "Retrieves the nutrition plan currently in ACTIVE status for the given patient. "
              + "Accessible by ADMIN, NUTRITIONIST, or the target patient.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active nutrition plan retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NutritionPlanDetailDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Patient has no active nutrition plan",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Patient has no active nutrition plan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or #patientId == authentication.principal.id")
  public ResponseEntity<NutritionPlanDetailDTO> findActiveByPatient(@PathVariable Long patientId) {
    return ResponseEntity.ok(nutritionPlanService.findActiveByPatient(patientId));
  }
}
