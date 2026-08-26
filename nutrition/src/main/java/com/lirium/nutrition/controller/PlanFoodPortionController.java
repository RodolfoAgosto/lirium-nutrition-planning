package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.service.PlanFoodPortionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/plan-food-portions")
@RequiredArgsConstructor
@Tag(name = "Plan Food Portions", description = "Endpoints for retrieving planned food portion details within plan meals")
@SecurityRequirement(name = "bearerAuth")
public class PlanFoodPortionController {

    private final PlanFoodPortionService service;

    @Operation(
            summary = "Get plan food portions by plan meal ID",
            description = "Retrieves all planned food portions assigned to a specific plan meal. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Food portions retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlanFoodPortionResponseDTO.class))
                    )
            )
    })
    @GetMapping("/meal/{planMealId}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planFoodPortionSecurity.isMealOwner(#planMealId, authentication)")
    public ResponseEntity<List<PlanFoodPortionResponseDTO>> getByMeal(@P("planMealId") @PathVariable @Positive Long planMealId) {
        return ResponseEntity.ok(service.getByPlanMeal(planMealId));
    }

    @Operation(
            summary = "Get plan food portion by ID",
            description = "Retrieves details of a specific planned food portion. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Food portion details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlanFoodPortionResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planFoodPortionSecurity.isPortionOwner(#id, authentication)")
    public ResponseEntity<PlanFoodPortionResponseDTO> getById(@P("id") @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

}