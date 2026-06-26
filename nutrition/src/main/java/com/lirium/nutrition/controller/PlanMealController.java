package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;

import com.lirium.nutrition.service.PlanMealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/plan-meals")
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

        log.info("Creating plan meal for dailyPlanDayId={}", dto.dailyPlanId());
        log.debug("PlanMeal create payload={}", dto);
        PlanMealResponseDTO response = service.create(dto);
        log.info("Plan meal created successfully for dailyPlanDayId={}", dto.dailyPlanId());
        return response;

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        log.info("Deleting plan meal id={}", id);
        service.delete(id);
        log.info("Plan meal deleted successfully id={}", id);

    }

    @PostMapping("/{mealId}/portions")
    public PlanMealResponseDTO addPortion(@PathVariable Long mealId, @RequestBody FoodPortionAddRequestDTO dto) {
            return service.addPortion(mealId, dto);
    }

    @DeleteMapping("/{mealId}/portions/{portionId}")
    public PlanMealResponseDTO removePortion(@PathVariable Long mealId,
                                             @PathVariable Long portionId) {
        return service.removePortion(mealId, portionId);
    }

    @PatchMapping("/{mealId}/portions/{portionId}")
    public PlanMealResponseDTO updatePortion(@PathVariable Long mealId,
                                             @PathVariable Long portionId,
                                             @RequestBody PlanFoodPortionUpdateFoodRequestDTO dto) {
        return service.updatePortion(mealId, portionId, dto);
    }


}