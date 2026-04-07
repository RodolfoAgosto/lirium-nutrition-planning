package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.AddFoodPortionRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-records")
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;
    private final AdherenceReportService adherenceReportService;

    @GetMapping("/today/{patientId}")
    public ResponseEntity<DailyRecordResponseDTO> getOrCreateToday(@PathVariable Long patientId) {
        return ResponseEntity.ok(dailyRecordService.getOrCreateToday(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyRecordResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dailyRecordService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DailyRecordResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(dailyRecordService.getByPatient(patientId));
    }

    @PatchMapping("/meals/{mealRecordId}")
    public ResponseEntity<MealRecordResponseDTO> updateMeal(
            @PathVariable Long mealRecordId,
            @RequestBody MealRecordUpdateRequestDTO request) {
        return ResponseEntity.ok(dailyRecordService.updateMeal(mealRecordId, request));
    }

    @PostMapping("/meals/{mealRecordId}/portions")
    public ResponseEntity<MealRecordResponseDTO> addPortion(
            @PathVariable Long mealRecordId,
            @RequestBody AddFoodPortionRequestDTO request) {
        return ResponseEntity.ok(dailyRecordService.addPortion(mealRecordId, request));
    }

    @DeleteMapping("/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}")
    public ResponseEntity<Void> removePortion(
            @PathVariable Long dailyRecordId,
            @PathVariable Long mealRecordId,
            @PathVariable Long portionId) {
        dailyRecordService.removePortion(dailyRecordId, mealRecordId, portionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}/adherence")
    public ResponseEntity<AdherenceReportDTO> getAdherence(
            @PathVariable Long patientId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(adherenceReportService.getAdherence(patientId, from, to));
    }

    @GetMapping("/patient/{patientId}/nutrition-comparison")
    public ResponseEntity<NutritionComparisonReportDTO> getNutritionComparison(
            @PathVariable Long patientId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(
                dailyRecordService.getNutritionComparison(patientId, from, to));
    }

}