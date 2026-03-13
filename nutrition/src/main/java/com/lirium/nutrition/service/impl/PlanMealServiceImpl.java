package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanMealMapper;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import com.lirium.nutrition.service.PlanMealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanMealServiceImpl implements PlanMealService {

    private final PlanMealRepository repository;
    private final DailyPlanRepository dailyPlanRepository;


    @Override
    public PlanMealResponseDTO getById(Long id) {

        PlanMeal meal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan meal not found"));

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

        DailyPlan dailyPlan = dailyPlanRepository.findById(dto.dailyPlanId())  // ← faltaba ) acá
                .orElseThrow(() ->
                        new ResourceNotFoundException("Daily Plan not found", dto.dailyPlanId())
                );

        PlanMeal   entity = PlanMealMapper.toEntity(dto, dailyPlan);

        PlanMeal saved = repository.save(entity);

        return PlanMealMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}