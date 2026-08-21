package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.service.PlanFoodPortionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plan-food-portions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlanFoodPortionController {

    private final PlanFoodPortionService service;

    @GetMapping("/meal/{planMealId}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planFoodPortionSecurity.isMealOwner(#planMealId, authentication)")
    public ResponseEntity<List<PlanFoodPortionResponseDTO>> getByMeal(@P("planMealId") @PathVariable @Positive Long planMealId) {
        return ResponseEntity.ok(service.getByPlanMeal(planMealId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planFoodPortionSecurity.isPortionOwner(#id, authentication)")
    public ResponseEntity<PlanFoodPortionResponseDTO> getById(@P("id") @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

}