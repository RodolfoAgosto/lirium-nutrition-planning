package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.service.FoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public Set<FoodSummaryDTO> findAll() {
        return foodService.findAll();
    }

    @GetMapping("/{id}")
    public FoodResponseDTO findById(@PathVariable Long id) {
        return foodService.findById(id);
    }

    @PostMapping
    public FoodSummaryDTO create(@RequestBody FoodCreateRequestDTO dto) {

        log.info("Creating food name={}", dto.name());
        if (log.isDebugEnabled()) {
            log.debug("Food create payload={}", dto);
        }
        FoodSummaryDTO response = foodService.create(dto);
        log.info("Food created successfully name={}", dto.name());
        return response;

    }

    @PutMapping("/{id}")
    public FoodSummaryDTO update(
            @PathVariable Long id,
            @RequestBody FoodUpdateRequestDTO dto) {

        log.info("Updating food id={}", id);
        if (log.isDebugEnabled()) {
            log.debug("Food update payload={}", dto.toString());
        }
        FoodSummaryDTO response = foodService.update(id, dto);
        log.info("Food updated successfully id={}", id);
        return response;

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        log.info("Deleting food id={}", id);
        foodService.deleteById(id);
        log.info("Food deleted successfully id={}", id);

    }
}