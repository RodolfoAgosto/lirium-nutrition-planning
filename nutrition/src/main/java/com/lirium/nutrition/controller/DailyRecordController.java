package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-records")
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;
    private final AdherenceReportService adherenceReportService;

    @GetMapping("/today/{patientId}")
    @PreAuthorize("""
        hasAnyRole('ADMIN','NUTRITIONIST') or @patientSecurity.isOwner(#patientId, authentication)
        """)
    public ResponseEntity<DailyRecordResponseDTO> getOrCreateToday(
            @PathVariable Long patientId) {

        log.info("Fetching or creating daily record for patientId={}", patientId);
        DailyRecordResponseDTO response = dailyRecordService.getOrCreateToday(patientId);
        log.info("Daily record ready for patientId={}", patientId);
        return ResponseEntity.ok(response);
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
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @dailyRecordServiceImpl.isMealRecordOwnedByUser(#mealRecordId, authentication.principal.id)")
    public ResponseEntity<MealRecordResponseDTO> addPortion(
            @PathVariable Long mealRecordId,
            @Valid @RequestBody FoodPortionAddRequestDTO request) {

        log.info("Adding portion to mealRecordId={} with foodId={}", mealRecordId, request.foodId());
        log.debug("Portion payload={}", request.toString());
        MealRecordResponseDTO response = dailyRecordService.addPortion(mealRecordId, request);
        log.info("Portion added successfully to mealRecordId={}", mealRecordId);
        return ResponseEntity.ok(response);

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