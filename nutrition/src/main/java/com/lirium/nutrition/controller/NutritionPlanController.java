package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequest;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition-plans")
public class NutritionPlanController {

    private final NutritionPlanGenerator nutritionPlanGenerator;
    private final NutritionPlanService nutritionPlanService;

    @PostMapping("/generate/{patientId}")
    public ResponseEntity<NutritionPlanDetailDTO> generate(@PathVariable Long patientId) {

        NutritionPlanDetailDTO dto = nutritionPlanGenerator.generate(patientId);

        return ResponseEntity.status(201).body(dto);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<NutritionPlanDetailDTO> complete(
            @PathVariable Long id,
            @RequestBody CompleteNutritionPlanRequest request) {
        return ResponseEntity.ok(nutritionPlanService.complete(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        nutritionPlanService.activatePlan(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/generate-from-template/{patientId}/{templateId}")
    public NutritionPlanDetailDTO generateFromTemplate(
            @PathVariable Long patientId,
            @PathVariable Long templateId) {
        return nutritionPlanGenerator.generateFromTemplate(patientId, templateId);
    }
}