package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.exception.DuplicateFoodException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.FoodMapper;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.mapper.PlanMealMapper;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.PlanMealService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanMealServiceImpl implements PlanMealService {

    private final PlanMealRepository repository;
    private final DailyPlanRepository dailyPlanRepository;
    private final FoodServiceImpl foodService;
    private final PlanFoodPortionServiceImpl planFoodPortionService;

    @Override
    public PlanMealResponseDTO getById(Long id) {

        PlanMeal meal = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan Meal not found", id));

        return PlanMealMapper.toResponse(meal);
    }

    @Override
    public List<PlanMealSummaryDTO> getByPlanDay(Long planDayId) {

        return repository.findByDailyPlanId(planDayId)
                .stream()
                .map(PlanMealMapper::toSummary)
                .toList();
    }

    @Override
    public PlanMealResponseDTO create(PlanMealCreateRequestDTO dto) {

        log.info("Creating plan meal dailyPlanId={} type={}", dto.dailyPlanId(), dto.type());

        DailyPlan dailyPlan = dailyPlanRepository.findById(dto.dailyPlanId())
                .orElseThrow(() -> {
                    log.warn("Daily plan not found id={}", dto.dailyPlanId());
                    return new ResourceNotFoundException("Daily Plan", dto.dailyPlanId());
                });

            log.debug("Plan meal payload dailyPlanId={} type={}",
                    dto.dailyPlanId(),
                    dto.type()
            );

        PlanMeal   entity = PlanMealMapper.toEntity(dto, dailyPlan);

        PlanMeal saved = repository.save(entity);

        log.info("Plan meal created successfully id={} dailyPlanId={}", saved.getId(), dto.dailyPlanId());

        return PlanMealMapper.toResponse(saved);

    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public PlanMealResponseDTO addPortion(Long mealId, FoodPortionAddRequestDTO dto) {

        PlanMeal planMeal = repository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanMeal", mealId));

        boolean exists = planMeal.getFoodPortions().stream()
                .anyMatch(fp -> fp.getFood().getId().equals(dto.foodId()));

        if (exists) {
            throw new DuplicateFoodException(
                    String.format("The food with id %d already exists.", dto.foodId()));
        }

        Food food = foodService.findEntityById(dto.foodId());

        planMeal.addFoodPortion(
                PlanFoodPortionMapper.toEntity(dto, planMeal, food )
        );

        return PlanMealMapper.toResponse(planMeal);
    }

    @Override
    @Transactional
    public PlanMealResponseDTO removePortion(Long mealId, Long portionId) {

        PlanMeal planMeal = repository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanMeal", mealId));

        PlanFoodPortion planFoodPortion = planFoodPortionService.findEntityById(portionId);

        planMeal.removeFoodPortion(planFoodPortion);

        return PlanMealMapper.toResponse(planMeal);
    }

    @Override
    @Transactional
    public PlanMealResponseDTO updatePortion(Long mealId, Long portionId,
                                             PlanFoodPortionUpdateFoodRequestDTO dto) {

        PlanMeal meal = repository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanMeal", mealId));

        PlanFoodPortion portion = meal.getFoodPortions().stream()
                .filter(p -> p.getId().equals(portionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Portion", portionId));

        // cambio de food si viene
        if (dto.foodId() != null) {

            boolean exists = meal.getFoodPortions().stream()
                    .anyMatch(p -> p.getFood().getId().equals(dto.foodId()));

            if (exists) {
                throw new DuplicateFoodException("Food already exists in meal");
            }

            Food food = foodService.findEntityById(dto.foodId());
            portion.changeFood(food);
        }

        // cambio de cantidad si viene
        if (dto.quantity() != null) {
            portion.changeQuantity(dto.quantity());
        }

        return PlanMealMapper.toResponse(meal);
    }


}