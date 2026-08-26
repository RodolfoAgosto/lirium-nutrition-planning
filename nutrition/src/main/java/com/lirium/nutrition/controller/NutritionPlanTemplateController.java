package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.service.NutritionPlanTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag( name = "Nutrition Plan Templates", description = "Endpoints for creating, retrieving, updating, and managing nutritional plan templates."
)
public class NutritionPlanTemplateController {

    private final NutritionPlanTemplateService service;

    @Operation(
            summary = "Get all nutrition plan templates",
            description = "Retrieves a summary list of all available nutrition plan templates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Templates retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = NutritionPlanTemplateSummaryDTO.class))
                    )
            ),
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public List<NutritionPlanTemplateSummaryDTO> getAll() {
        return service.getAll();
    }

    @Operation(
            summary = "Get nutrition plan template by ID",
            description = "Retrieves full details of a specific nutrition plan template by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NutritionPlanTemplateResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid template ID", content = @Content),
    })
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

    @Operation(
            summary = "Delete nutrition plan template",
            description = "Deletes an existing nutrition plan template by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Template deleted successfully"),
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "ID must be a positive number") Long id
    ){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update nutrition plan template",
            description = "Updates an existing nutrition plan template by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NutritionPlanTemplateResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "422", description = "Macro percentages do not sum to 100%", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
    public NutritionPlanTemplateResponseDTO update(
            @Valid @PathVariable Long id,
            @RequestBody NutritionPlanTemplateUpdateRequestDTO dto
    ) {
        return service.update(id, dto);
    }

}