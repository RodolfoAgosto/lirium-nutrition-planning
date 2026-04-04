package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.service.PlanFoodPortionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan-food-portions")
@RequiredArgsConstructor
public class PlanFoodPortionController {

    private final PlanFoodPortionService service;

    @GetMapping("/meal/{planMealId}")
    public List<PlanFoodPortionResponseDTO> getByMeal(@PathVariable Long planMealId) {

        return service.getByPlanMeal(planMealId);
    }

    @GetMapping("/{id}")
    public PlanFoodPortionResponseDTO getById(@PathVariable Long id) {

        return service.getById(id);
    }

    @PostMapping
    public PlanFoodPortionResponseDTO create(@Valid @RequestBody PlanFoodPortionCreateRequestDTO dto) {

        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlanFoodPortionResponseDTO> update(
            @PathVariable Long id,
            @RequestBody UpdatePlanFoodPortionRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

}