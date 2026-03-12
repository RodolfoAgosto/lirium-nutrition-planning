package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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
        return foodService.create(dto);
    }

    @PutMapping("/{id}")
    public FoodSummaryDTO update(
            @PathVariable Long id,
            @RequestBody FoodUpdateRequestDTO dto) {

        return foodService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        foodService.deleteById(id);
    }
}