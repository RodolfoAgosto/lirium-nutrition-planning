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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        PlanMeal meal = planMealRepository.findById(dto.mealId())
                .orElseThrow(() -> new ResourceNotFoundException("PlanMeal", dto.mealId()));

        Food food = foodRepository.findById(dto.foodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", dto.foodId()));

        PlanFoodPortion portion = PlanFoodPortionMapper.toEntity(dto, meal, food);

        PlanFoodPortion saved = repository.save(portion);

        return PlanFoodPortionMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public PlanFoodPortionResponseDTO update(Long id, UpdatePlanFoodPortionRequestDTO request) {

        PlanFoodPortion portion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanFoodPortion", id));

        if (portion.getMeal().getDailyPlan().getNutritionPlan().getStatus() != PlanStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT plans can be modified");
        }

        if (request.foodId() != null) {
            Food newFood = foodRepository.findById(request.foodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food", request.foodId()));

            if (newFood.getCategory() != portion.getFood().getCategory()) {
                throw new IllegalArgumentException(
                        "Food must be of the same category: " + portion.getFood().getCategory()
                );
            }
            portion.changeFood(newFood);
        }

        if (request.quantity() != null) {
            portion.changeQuantity(request.quantity());
        }

        return PlanFoodPortionMapper.toResponse(repository.save(portion));
    }

}
