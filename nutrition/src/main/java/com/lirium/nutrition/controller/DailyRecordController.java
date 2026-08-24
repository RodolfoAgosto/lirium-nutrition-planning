package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-records")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Daily Records", description = "Endpoints for managing and tracking daily nutrition records")
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;
    private final AdherenceReportService adherenceReportService;

    @Operation(
            summary = "Ensure/Fetch daily record for a patient (Get-or-Create)",
            description = """
            Fetches or creates the daily record (`DailyRecord`) for the specified date.
            - If it **already exists**, returns the existing record (`200 OK`).
            - If it **does not exist**, generates the daily record automatically based on the prescribed meals in the patient's active nutrition plan (`201 Created`).
            - If the `date` parameter is omitted, it defaults to today's date (`LocalDate.now()`).
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Daily record already exists and was retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DailyRecordResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Daily record was successfully generated from the active nutrition plan",
                    content = @Content(schema = @Schema(implementation = DailyRecordResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date (date is in the future or prior to the active nutrition plan start date)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (Missing or expired JWT token)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (Patient is attempting to access another user's daily record)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Patient profile not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable Entity (Patient does not have an active nutrition plan to generate records from)",
                    content = @Content
            )
    })
    @PostMapping("/patient/{patientId}/ensure")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
    public ResponseEntity<DailyRecordResponseDTO> ensureDailyRecord(
            @Parameter(description = "ID of the patient profile", example = "5", required = true)
            @PathVariable @Positive Long patientId,
            @Parameter(description = "Date for the daily record (ISO Format: YYYY-MM-DD). Defaults to TODAY if omitted.", example = "2026-08-24")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        log.info("Ensuring daily record exists for patientId={} date={}", patientId, targetDate);

        boolean existed = dailyRecordService.existsForPatientAndDate(patientId, targetDate);

        DailyRecordResponseDTO response = dailyRecordService.getOrCreateForDate(patientId, targetDate);

        if (existed) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordServiceImpl.isDailyRecordOwnedByUser(#id, authentication.principal.id)")
    public ResponseEntity<DailyRecordResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dailyRecordService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
    public ResponseEntity<List<DailyRecordResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(dailyRecordService.getByPatient(patientId));
    }

    @PatchMapping("/meals/{mealRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordServiceImpl.isMealRecordOwnedByUser(#mealRecordId, authentication.principal.id)")
    public ResponseEntity<MealRecordResponseDTO> updateMeal(
            @PathVariable Long mealRecordId,
            @Valid @RequestBody MealRecordUpdateRequestDTO request) {

        log.info("Updating mealRecordId={} (request received)", mealRecordId);
        log.debug("Meal update payload={}", request);
        MealRecordResponseDTO response = dailyRecordService.updateMeal(mealRecordId, request);
        log.info("Meal updated successfully mealRecordId={}", mealRecordId);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/meals/{mealRecordId}/portions")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordSecurity.isMealRecordOwner(#mealRecordId, authentication)")
    @Operation(
            summary = "Add food portion to meal record",
            description = "Registers actual food intake for a specific meal record. Allowed for ADMIN, NUTRITIONIST or the record owner."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Portion added successfully to meal record",
                    content = @Content(schema = @Schema(implementation = MealRecordResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payload or non-positive quantity"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to modify this meal record"),
            @ApiResponse(responseCode = "404", description = "Meal record or Food not found")
    })
    public ResponseEntity<MealRecordResponseDTO> addPortion(
            @PathVariable("mealRecordId")
            @NotNull(message = "Meal record ID is required")
            @Positive(message = "Meal record ID must be positive")
            Long mealRecordId,
            @Valid @RequestBody FoodPortionAddRequestDTO request
    ) {

        log.info("Adding portion to mealRecordId={} with foodId={}", mealRecordId, request.foodId());
        log.debug("Portion payload={}", request.toString());
        MealRecordResponseDTO response = dailyRecordService.addPortion(mealRecordId, request);
        log.info("Portion added successfully to mealRecordId={}", mealRecordId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordServiceImpl.isDailyRecordOwnedByUser(#dailyRecordId, authentication.principal.id)")
    public ResponseEntity<Void> removePortion(
            @PathVariable Long dailyRecordId,
            @PathVariable Long mealRecordId,
            @PathVariable Long portionId) {

        log.info("Removing portionId={} from mealRecordId={} dailyRecordId={}", portionId, mealRecordId, dailyRecordId);
        dailyRecordService.removePortion(dailyRecordId, mealRecordId, portionId);
        log.info("Portion removed successfully portionId={}", portionId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/patient/{patientId}/adherence")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
    public ResponseEntity<AdherenceReportDTO> getAdherence(
            @PathVariable Long patientId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        log.info("Generating adherence report for patientId={} from={} to={}", patientId, from, to);
        AdherenceReportDTO response = adherenceReportService.getAdherence(patientId, from, to);
        log.info("Adherence report generated for patientId={}", patientId);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/patient/{patientId}/nutrition-comparison")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
    public ResponseEntity<NutritionComparisonReportDTO> getNutritionComparison(
            @PathVariable Long patientId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        log.info("Generating nutrition comparison for patientId={} from={} to={}", patientId, from, to);
        NutritionComparisonReportDTO response = dailyRecordService.getNutritionComparison(patientId, from, to);
        log.info("Nutrition comparison generated for patientId={}", patientId);
        return ResponseEntity.ok(response);

    }

}