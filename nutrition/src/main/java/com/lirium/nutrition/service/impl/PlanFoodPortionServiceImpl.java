package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.dto.request.UpdatePlanFoodPortionRequestDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.repository.*;
import com.lirium.nutrition.service.PlanFoodPortionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanFoodPortionServiceImpl implements PlanFoodPortionService {

    private final PlanFoodPortionRepository repository;
    private final PlanMealRepository planMealRepository;
    private final FoodRepository foodRepository;


    @Override
    public List<PlanFoodPortionResponseDTO> getByPlanMeal(Long planMealId) {

        return repository.findByMealId(planMealId)
                .stream()
                .map(PlanFoodPortionMapper::toResponse)
                .toList();
    }

    @Override
    public PlanFoodPortionResponseDTO getById(Long id) {

        PlanFoodPortion portion = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food portion not found"));

        return PlanFoodPortionMapper.toResponse(portion);
    }

    @Override
    public PlanFoodPortionResponseDTO create(PlanFoodPortionCreateRequestDTO dto) {

        log.info("Creating portion mealId={} foodId={}", dto.mealId(), dto.foodId());

        PlanMeal meal = planMealRepository.findById(dto.mealId())
                .orElseThrow(() -> {
                    log.warn("PlanMeal not found id={}", dto.mealId());
                    return new ResourceNotFoundException("PlanMeal", dto.mealId());
                });

        Food food = foodRepository.findById(dto.foodId())
                .orElseThrow(() -> {
                    log.warn("Food not found id={}", dto.foodId());
                    return new ResourceNotFoundException("Food", dto.foodId());
                });

        PlanFoodPortion portion = PlanFoodPortionMapper.toEntity(dto, meal, food);

        PlanFoodPortion saved = repository.save(portion);

        log.info("Portion created successfully id={} mealId={} foodId={}", saved.getId(), meal.getId(), food.getId());

        return PlanFoodPortionMapper.toResponse(saved);

    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public PlanFoodPortionResponseDTO update(Long id, UpdatePlanFoodPortionRequestDTO request) {

        log.info("Updating portion id={}", id);

        PlanFoodPortion portion = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Portion not found id={}", id);
                    return new ResourceNotFoundException("PlanFoodPortion", id);
                });

        if (portion.getMeal().getDailyPlan().getNutritionPlan().getStatus() != PlanStatus.DRAFT) {
            log.warn("Attempt to modify portion id={} on non-DRAFT plan", id);
            throw new IllegalStateException("Only DRAFT plans can be modified");
        }

        if (log.isDebugEnabled()) {
            log.debug("Update payload id={} foodId={} quantity={}",
                    id, request.foodId(), request.quantity());
        }

        if (request.foodId() != null) {
            Food newFood = foodRepository.findById(request.foodId())
                    .orElseThrow(() -> {
                        log.warn("Food not found id={}", request.foodId());
                        return new ResourceNotFoundException("Food", request.foodId());
                    });

            if (newFood.getCategory() != portion.getFood().getCategory()) {
                log.warn("Invalid food category change portionId={} from={} to={}",
                        id,
                        portion.getFood().getCategory(),
                        newFood.getCategory());
                throw new IllegalArgumentException(
                        "Food must be of the same category: " + portion.getFood().getCategory()
                );
            }

            portion.changeFood(newFood);

        }

        if (request.quantity() != null) {
            portion.changeQuantity(request.quantity());
        }

        PlanFoodPortion saved = repository.save(portion);

        log.info("Portion updated successfully id={}", id);


        return PlanFoodPortionMapper.toResponse(saved);

    }

}
