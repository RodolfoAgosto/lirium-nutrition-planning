package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.RestrictionCatalogUpdateDTO;
import com.lirium.nutrition.dto.request.RestrictionCreateRequestDTO;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.service.RestrictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/restrictions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dietary Restrictions", description = "Endpoints for managing global dietary restrictions catalog (dietary, intolerances, pathological)")
@Validated
public class RestrictionController {

    private final RestrictionService restrictionService;

    @Operation(
            operationId = "getAllRestrictions",
            summary = "Get all dietary restrictions",
            description = "Retrieves a summary list of all dietary restrictions registered in the catalog."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dietary restrictions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RestrictionSummaryDTO.class))
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST', 'PATIENT')")
    public ResponseEntity<Set<RestrictionSummaryDTO>> findAll() {
        Set<RestrictionSummaryDTO> restrictions = restrictionService.findAll();
        return ResponseEntity.ok(restrictions);
    }

    @Operation(
            operationId = "createRestriction",
            summary = "Create a new dietary restriction",
            description = "Creates a new entry in the global dietary restrictions catalog. Restricted to ADMIN users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Dietary restriction created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestrictionSummaryDTO.class)
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestrictionSummaryDTO> create(@Valid @RequestBody RestrictionCreateRequestDTO request) {

        log.info("Creating restriction name={}", request.name());
        log.debug("Restriction create payload={}", request.toString());
        RestrictionSummaryDTO response = restrictionService.create(request);
        log.info("Restriction created successfully name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            operationId = "getRestrictionById",
            summary = "Get dietary restriction details by ID",
            description = "Retrieves full detailed information of a specific dietary restriction by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dietary restriction found and retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestrictionResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestrictionResponseDTO> findById(
            @PathVariable("id")
            @NotNull(message = "ID is required")
            @Positive(message = "ID must be a positive number")
            Long id ){
        log.info("Fetching restriction id={}", id);
        RestrictionResponseDTO response = restrictionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            operationId = "updateRestriction",
            summary = "Update a dietary restriction",
            description = "Updates an existing dietary restriction in the catalog. Restricted to ADMIN users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dietary restriction updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestrictionSummaryDTO.class)
                    )
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestrictionSummaryDTO> update(
            @PathVariable("id")
            @NotNull(message = "ID is required")
            @Positive(message = "ID must be a positive number")
            Long id,
            @Valid @RequestBody RestrictionCatalogUpdateDTO request) {

        log.info("Updating restriction id={}", id);
        log.debug("Restriction update payload={}", request.toString());
        RestrictionSummaryDTO response = restrictionService.update(id, request);
        log.info("Restriction updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

}