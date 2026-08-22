package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.service.FoodService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Validated
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST', 'PATIENT')")
    public Set<FoodSummaryDTO> findAll() {
        return foodService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST', 'PATIENT')")
    public FoodResponseDTO findById(@PathVariable @Positive Long id) {
        return foodService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FoodSummaryDTO> create(@Valid @RequestBody FoodCreateRequestDTO dto) {

        log.info("Creating food name={}", dto.name());
        log.debug("Food create payload={}", dto);
        FoodSummaryDTO response = foodService.create(dto);
        log.info("Food created successfully name={}", dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST')")
    public ResponseEntity<FoodSummaryDTO> update(
            @PathVariable @Positive(message = "ID must be greater than 0") Long id,
            @Valid @RequestBody FoodUpdateRequestDTO dto) {

        log.info("Updating food id={}", id);
        log.debug("Food update payload={}", dto.toString());
        FoodSummaryDTO response = foodService.update(id, dto);
        log.info("Food updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('NUTRITIONIST', 'ADMIN')")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") @NotNull(message = "ID is required") @Positive(message = "ID must be a positive number")
            Long id
    ){
        log.info("Deleting food id={}", id);
        foodService.deleteById(id);
        log.info("Food deleted successfully id={}", id);
        return ResponseEntity.noContent().build();
    }
}