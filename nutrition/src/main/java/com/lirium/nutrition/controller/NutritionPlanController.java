package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequest;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition-plans")
public class NutritionPlanController {

    private final NutritionPlanGenerator nutritionPlanGenerator;
    private final NutritionPlanService nutritionPlanService;

    @PostMapping("/generate/{patientId}")
    public ResponseEntity<NutritionPlanDetailDTO> generate(@PathVariable Long patientId) {

        log.info("Generating nutrition plan for patientId={}", patientId);
        NutritionPlanDetailDTO dto = nutritionPlanGenerator.generate(patientId);
        log.info("Nutrition plan generated successfully for patientId={}", patientId);
        return ResponseEntity.status(201).body(dto);

    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<NutritionPlanDetailDTO> complete(
            @PathVariable Long id,
            @RequestBody CompleteNutritionPlanRequest request) {

        log.info("Completing nutrition plan id={}", id);
        if (log.isDebugEnabled()) {
            log.debug("Complete plan payload={}", request.toString());
        }
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
    public ResponseEntity<NutritionPlanDetailDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(nutritionPlanService.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NutritionPlanSummaryDTO>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(nutritionPlanService.findByPatient(patientId));
    }

}