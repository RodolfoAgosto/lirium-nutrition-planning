package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;

import com.lirium.nutrition.service.PlanMealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan-meals")
@RequiredArgsConstructor
public class PlanMealController {

    private final PlanMealService service;

    @GetMapping("/{id}")
    public PlanMealResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/day/{planDayId}")
    public List<PlanMealSummaryDTO> getByPlanDay(@PathVariable Long planDayId) {
        return service.getByPlanDay(planDayId);
    }

    @PostMapping
    public PlanMealResponseDTO create(@RequestBody PlanMealCreateRequestDTO dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}