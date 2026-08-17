package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.service.NutritionPlanTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition-plan-templates")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Nutrition Plan Templates",
        description = "Endpoints for creating, retrieving, updating, and managing nutritional plan templates."
)
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


    @Operation(
            summary = "Create basic plan template",
            description = "Creates a new basic plan template. Validates that macronutrient percentages sum up to exactly 100%."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Template created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or malformed JSON"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Macro percentages do not sum to 100%"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public ResponseEntity<NutritionPlanTemplateResponseDTO> create(@Valid @RequestBody NutritionPlanTemplateCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "ID must be a positive number") Long id
    ){
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