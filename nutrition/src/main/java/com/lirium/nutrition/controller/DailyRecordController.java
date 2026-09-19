package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.exception.ApiError;
import com.lirium.nutrition.service.DailyRecordService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/daily-records")
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Daily Records",
    description = "Endpoints for managing and tracking daily nutrition records")
public class DailyRecordController {

  private final DailyRecordService dailyRecordService;

  @Operation(
      operationId = "getDailyRecordById",
      summary = "Get daily record by ID",
      description =
          "Retrieves full details of a specific daily record. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Daily record retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DailyRecordResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid daily record ID parameter",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
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
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Daily record not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordSecurity.isDailyRecordOwner(#id, authentication)")
  public ResponseEntity<DailyRecordResponseDTO> getDailyRecordById(
      @PathVariable("id")
          @NotNull(message = "Daily record ID is required")
          @Positive(message = "Daily record ID must be positive")
          Long id) {
    return ResponseEntity.ok(dailyRecordService.getById(id));
  }

  @Operation(
      operationId = "updateMealRecord",
      summary = "Update meal record",
      description = "Updates the state or details of a specific meal record within a daily record.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Meal record updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MealRecordResponseDTO.class))),
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
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Meal record not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @PatchMapping("/meals/{mealRecordId}")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordSecurity.isMealRecordOwner(#mealRecordId, authentication)")
  public ResponseEntity<MealRecordResponseDTO> updateMealRecord(
      @PathVariable("mealRecordId")
          @NotNull(message = "Meal record ID is required")
          @Positive(message = "Meal record ID must be positive")
          Long mealRecordId,
      @Valid @RequestBody MealRecordUpdateRequestDTO request) {

    log.info("Updating mealRecordId={} (request received)", mealRecordId);
    log.debug("Meal update payload={}", request);
    MealRecordResponseDTO response = dailyRecordService.updateMeal(mealRecordId, request);
    log.info("Meal updated successfully mealRecordId={}", mealRecordId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/meals/{mealRecordId}/portions")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordSecurity.isMealRecordOwner(#mealRecordId, authentication)")
  @Operation(
      operationId = "addMealRecordPortion",
      summary = "Add food portion to meal record",
      description =
          "Registers actual food intake for a specific meal record. Allowed for ADMIN, NUTRITIONIST or the record owner.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Portion added successfully to meal record",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MealRecordResponseDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid payload or non-positive quantity",
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
        description = "Forbidden - Not authorized to modify this meal record",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Meal record or Food not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<MealRecordResponseDTO> addFoodPortion(
      @PathVariable("mealRecordId")
          @NotNull(message = "Meal record ID is required")
          @Positive(message = "Meal record ID must be positive")
          Long mealRecordId,
      @Valid @RequestBody FoodPortionAddRequestDTO request) {

    log.info("Adding portion to mealRecordId={} with foodId={}", mealRecordId, request.foodId());
    log.debug("Portion payload={}", request.toString());
    MealRecordResponseDTO response = dailyRecordService.addPortion(mealRecordId, request);
    log.info("Portion added successfully to mealRecordId={}", mealRecordId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      operationId = "removeMealRecordPortion",
      summary = "Remove a food portion",
      description =
          "Removes a specific food portion (FoodPortionRecord) from a meal (MealRecord) "
              + "within a daily record (DailyRecord). Marks the meal as overridden.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Food portion removed successfully"),
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
  @DeleteMapping("/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}")
  @PreAuthorize(
      "hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordSecurity.isMealRecordOwner(#mealRecordId, authentication)")
  public ResponseEntity<Void> removeFoodPortion(
      @PathVariable("dailyRecordId")
          @NotNull(message = "Daily record ID is required")
          @Positive(message = "Daily record ID must be positive")
          Long dailyRecordId,
      @PathVariable("mealRecordId")
          @NotNull(message = "Meal record ID is required")
          @Positive(message = "Meal record ID must be positive")
          Long mealRecordId,
      @PathVariable("portionId")
          @NotNull(message = "Portion record ID is required")
          @Positive(message = "Portion record ID must be positive")
          Long portionId) {

    log.info(
        "Removing portionId={} from mealRecordId={} dailyRecordId={}",
        portionId,
        mealRecordId,
        dailyRecordId);
    dailyRecordService.removePortion(dailyRecordId, mealRecordId, portionId);
    log.info("Portion removed successfully portionId={}", portionId);
    return ResponseEntity.noContent().build();
  }
}
