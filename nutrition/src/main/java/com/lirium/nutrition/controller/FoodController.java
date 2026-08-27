package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequiredArgsConstructor
@RequestMapping("/api/foods")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Food Catalog", description = "Endpoints for generating, retrieving, and managing food catalog.")
@Validated
public class FoodController {

    private final FoodService foodService;

    @Operation(
            operationId = "getAllFoods",
            summary = "Get all foods",
            description = "Returns all foods available in the nutrition catalog."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Foods retrieved successfully"
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST', 'PATIENT')")
    public Set<FoodSummaryDTO> getAllFoods() {
        return foodService.findAll();
    }

    @Operation(
            operationId = "getFoodById",
            summary = "Get food by ID",
            description = "Returns the complete information of a food from the nutrition catalog."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food retrieved successfully"
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST', 'PATIENT')")
    public FoodResponseDTO getFoodById(
            @Parameter(
                    description = "Unique identifier of the food",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "Food ID must be a positive number")
            Long id) {
        return foodService.findById(id);
    }

    @Operation(
            operationId = "createFood",
            summary = "Create a food",
            description = "Creates a new food in the nutrition catalog. Only administrators can perform this operation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Food created successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A food with the same unique attributes already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FoodSummaryDTO> createFood(@Valid @RequestBody FoodCreateRequestDTO dto) {

        log.info("Creating food name={}", dto.name());
        log.debug("Food create payload={}", dto);
        FoodSummaryDTO response = foodService.create(dto);
        log.info("Food created successfully name={}", dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            operationId = "updateFood",
            summary = "Update a food",
            description = "Updates an existing food in the nutrition catalog. Administrators and nutritionists can perform this operation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food updated successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The updated food conflicts with an existing catalog entry"
            )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST')")
    public ResponseEntity<FoodSummaryDTO> updateFood(
            @PathVariable @Positive(message = "ID must be greater than 0") Long id,
            @Valid @RequestBody FoodUpdateRequestDTO dto) {

        log.info("Updating food id={}", id);
        log.debug("Food update payload={}", dto.toString());
        FoodSummaryDTO response = foodService.update(id, dto);
        log.info("Food updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

    @Operation(
            operationId = "deleteFood",
            summary = "Delete a food",
            description = "Deletes a food from the nutrition catalog. Administrators and nutritionists can perform this operation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Food deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Food cannot be deleted because it is currently in use"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('NUTRITIONIST', 'ADMIN')")
    public ResponseEntity<Void> deleteFoodById(
            @PathVariable("id") @NotNull(message = "ID is required") @Positive(message = "ID must be a positive number")
            Long id
    ){
        log.info("Deleting food id={}", id);
        foodService.deleteById(id);
        log.info("Food deleted successfully id={}", id);
        return ResponseEntity.noContent().build();
    }
}