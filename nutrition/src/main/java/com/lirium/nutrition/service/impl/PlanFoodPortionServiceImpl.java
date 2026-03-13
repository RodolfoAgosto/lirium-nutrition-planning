package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.repository.*;
import com.lirium.nutrition.service.PlanFoodPortionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanFoodPortionServiceImpl implements PlanFoodPortionService {

    private final PlanFoodPortionRepository repository;
    private final PlanMealRepository planMealRepository;
    private final FoodRepository foodRepository;


    @Override
    public List<PlanFoodPortionResponseDTO> getByPlanMeal(Long planMealId) {

        return repository.findByPlanMealId(planMealId)
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
}
