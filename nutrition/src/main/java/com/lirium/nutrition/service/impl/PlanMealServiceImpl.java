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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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

        log.info("Creating plan meal dailyPlanId={} type={}", dto.dailyPlanId(), dto.type());

        DailyPlan dailyPlan = dailyPlanRepository.findById(dto.dailyPlanId())  // ← faltaba ) acá
                .orElseThrow(() -> {
                    log.warn("Daily plan not found id={}", dto.dailyPlanId());
                    return new ResourceNotFoundException("Daily Plan", dto.dailyPlanId());
                });

        if (log.isDebugEnabled()) {
            log.debug("Plan meal payload dailyPlanId={} type={}",
                    dto.dailyPlanId(),
                    dto.type()
            );
        }

        PlanMeal   entity = PlanMealMapper.toEntity(dto, dailyPlan);

        PlanMeal saved = repository.save(entity);

        log.info("Plan meal created successfully id={} dailyPlanId={}", saved.getId(), dto.dailyPlanId());

        return PlanMealMapper.toResponse(saved);

    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}