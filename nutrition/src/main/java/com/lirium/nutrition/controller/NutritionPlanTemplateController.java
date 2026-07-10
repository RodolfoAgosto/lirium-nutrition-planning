package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.service.NutritionPlanTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition-plan-templates")
@RequiredArgsConstructor
public class NutritionPlanTemplateController {

    private final NutritionPlanTemplateService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public List<NutritionPlanTemplateSummaryDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public NutritionPlanTemplateResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public ResponseEntity<NutritionPlanTemplateResponseDTO> create(
            @Valid @RequestBody NutritionPlanTemplateCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public NutritionPlanTemplateResponseDTO update(
            @Valid @PathVariable Long id,
            @RequestBody NutritionPlanTemplateUpdateRequestDTO dto
    ) {
        return service.update(id, dto);
    }

}