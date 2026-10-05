package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.exception.ApiError;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for a patient's daily records, nested under the owning patient's URL as the resource
 * hierarchy dictates (a patient HAS daily records). Operations addressed by a daily record's own id
 * (get by id, update a meal, add/remove a portion) live in {@link DailyRecordController} at {@code
 * /api/daily-records/{id}...} instead, since those don't need the patient in the path.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/patients/{patientId}/daily-records")
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Daily Records",
    description = "Endpoints for managing and tracking a patient's daily nutrition records")
public class PatientDailyRecordController {

  /** Reports without explicit dates cover this many days, ending today. */
  private static final int DEFAULT_REPORT_DAYS = 30;

  private static final String PATIENT_ID_DESCRIPTION =
      "ID of the patient. In the demo data, patient 2 has recorded days.";
  private static final String FROM_DESCRIPTION =
      "Start date (YYYY-MM-DD). Defaults to 29 days before 'to' (a 30-day range).";
  private static final String TO_DESCRIPTION = "End date (YYYY-MM-DD). Defaults to today.";

  private final DailyRecordService dailyRecordService;
  private final AdherenceReportService adherenceReportService;
  private final Clock clock;

  @Operation(
      operationId = "ensureDailyRecord",
      summary = "Ensure/Fetch daily record for a patient (Get-or-Create)",
      description =
          """
                                Fetches or creates the daily record (`DailyRecord`) for the specified date.
                                - If it **already exists**, returns the existing record (`200 OK`).
                                - If it **does not exist**, generates the daily record automatically based on the prescribed meals in the patient's active nutrition plan (`201 Created`).
                                - If the `date` parameter is omitted, it defaults to today's date (`LocalDate.now()`).
                                """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Daily record already exists and was retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DailyRecordResponseDTO.class))),
        @ApiResponse(
            responseCode = "201",
            description = "Daily record was successfully generated from the active nutrition plan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DailyRecordResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid date (date is in the future or prior to the active nutrition plan start date)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (Missing or expired JWT token)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (Patient is attempting to access another user's daily record)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Patient profile not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Unprocessable Entity (Patient does not have an active nutrition plan to generate records from)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @PostMapping("/ensure")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
  public ResponseEntity<DailyRecordResponseDTO> ensureDailyRecord(
      @Parameter(description = "ID of the patient profile", example = "2", required = true)
          @PathVariable
          @Positive
          Long patientId,
      @Parameter(
              description =
                  "Date for the daily record (ISO Format: YYYY-MM-DD). Defaults to TODAY if omitted.")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now(clock);

    log.info("Ensuring daily record exists for patientId={} date={}", patientId, targetDate);

    boolean existed = dailyRecordService.existsForPatientAndDate(patientId, targetDate);

    DailyRecordResponseDTO response = dailyRecordService.getOrCreateForDate(patientId, targetDate);

    if (existed) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
  }

  @Operation(
      operationId = "getDailyRecordsByPatient",
      summary = "Get daily records by patient ID",
      description =
          "Retrieves all daily records for a specific patient. Accessible by ADMIN, NUTRITIONIST, or the target PATIENT.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Daily records retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = DailyRecordResponseDTO.class)))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Missing or invalid JWT token.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. User lacks the required role.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
  public ResponseEntity<List<DailyRecordResponseDTO>> getDailyRecordByPatientId(
      @PathVariable("patientId")
          @NotNull(message = "Patient ID is required")
          @Positive(message = "Patient ID must be positive")
          Long patientId) {
    return ResponseEntity.ok(dailyRecordService.getByPatient(patientId));
  }

  @Operation(
      operationId = "getMealAdherence",
      summary = "Get patient adherence report",
      description =
          "For each day in the range, how many planned meals the patient was expected to log"
              + " and how many were followed as planned (not modified)."
              + " Days the patient never opened have hasRecord=false."
              + " Without from/to, the report covers the last 30 days up to today.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Adherence report generated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AdherenceReportDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date range",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT required",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Not authorized to view this patient's adherence",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping("/meal-adherence")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
  public ResponseEntity<AdherenceReportDTO> getAdherenceReport(
      @Parameter(description = PATIENT_ID_DESCRIPTION, example = "2")
          @NotNull(message = "Patient ID is required")
          @Positive(message = "Patient ID must be a positive number")
          @PathVariable("patientId")
          Long patientId,
      @Parameter(description = FROM_DESCRIPTION)
          @RequestParam(required = false)
          @PastOrPresent(message = "Start date cannot be in the future")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(description = TO_DESCRIPTION)
          @RequestParam(required = false)
          @PastOrPresent(message = "End date cannot be in the future")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {

    DateRange range = resolveRange(from, to);

    if (range.from().isAfter(range.to())) {
      throw new IllegalArgumentException("The 'from' date cannot be after 'to' date");
    }

    log.info(
        "Generating adherence report for patientId={} from={} to={}",
        patientId,
        range.from(),
        range.to());
    AdherenceReportDTO response =
        adherenceReportService.getAdherence(patientId, range.from(), range.to());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/nutrition-comparison")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)")
  @Operation(
      operationId = "getNutritionComparison",
      summary = "Get nutrition comparison report",
      description =
          "For each day in the range: what the active plan prescribes for that day of the week"
              + " (planned), what the patient recorded (consumed) and a 0-100 closeness score per"
              + " nutrient plus a weighted overall (calories 40%, each macro 20%). The plan's"
              + " daily targets are returned once as reference. Days with no record have"
              + " hasRecord=false and consumed/score null: no data, not zero intake. score is"
              + " also null when nothing was planned for that day."
              + " summary averages consumption over recorded days and scores over scored days;"
              + " days without data are excluded, and averages are null when there are none."
              + " Without from/to, the report covers the last 30 days up to today.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Report generated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NutritionComparisonReportDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid inputs or 'from' date is after 'to' date",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT required",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Not authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Patient or active plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  public ResponseEntity<NutritionComparisonReportDTO> getNutritionComparisonReport(
      @Parameter(description = PATIENT_ID_DESCRIPTION, example = "2")
          @NotNull(message = "Patient ID is required")
          @Positive(message = "Patient ID must be a positive number")
          @PathVariable("patientId")
          Long patientId,
      @Parameter(description = FROM_DESCRIPTION)
          @RequestParam(name = "from", required = false)
          @PastOrPresent(message = "Start date cannot be in the future")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(description = TO_DESCRIPTION)
          @RequestParam(name = "to", required = false)
          @PastOrPresent(message = "End date cannot be in the future")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {

    DateRange range = resolveRange(from, to);

    log.info(
        "Generating nutrition comparison for patientId={} from={} to={}",
        patientId,
        range.from(),
        range.to());
    NutritionComparisonReportDTO response =
        dailyRecordService.getNutritionComparison(patientId, range.from(), range.to());
    log.info("Nutrition comparison generated for patientId={}", patientId);
    return ResponseEntity.ok(response);
  }

  /** Fills in missing report dates: 'to' defaults to today, 'from' to 29 days before 'to'. */
  private DateRange resolveRange(LocalDate from, LocalDate to) {
    LocalDate end = (to != null) ? to : LocalDate.now(clock);
    LocalDate start = (from != null) ? from : end.minusDays(DEFAULT_REPORT_DAYS - 1L);
    return new DateRange(start, end);
  }

  private record DateRange(LocalDate from, LocalDate to) {}
}
