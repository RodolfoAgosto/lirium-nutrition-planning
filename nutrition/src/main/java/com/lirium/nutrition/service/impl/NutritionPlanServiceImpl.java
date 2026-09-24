package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.NutritionPlanCompleteRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.exception.NutritionPlanNotFoundException;
import com.lirium.nutrition.mapper.NutritionPlanMapper;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.service.NutritionPlanService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("nutritionPlanService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanServiceImpl implements NutritionPlanService {

  private final NutritionPlanRepository repository;
  private final Clock clock;

  @Transactional
  public NutritionPlanDetailDTO complete(Long id, NutritionPlanCompleteRequestDTO request) {

    NutritionPlan plan =
        repository.findById(id).orElseThrow(() -> new NutritionPlanNotFoundException(id));

    plan.complete(request.name(), request.description(), LocalDate.now(clock));

    return NutritionPlanMapper.toDetail(plan);
  }

  @Override
  @Transactional
  public NutritionPlanDetailDTO activatePlan(Long planId) {

    NutritionPlan newPlan =
        repository.findById(planId).orElseThrow(() -> new NutritionPlanNotFoundException(planId));

    Long patientId = newPlan.getPatientProfile().getId();

    Optional<NutritionPlan> previousActivePlan =
        repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);

    // Activate first: it validates the plan is DRAFT before anything else is changed
    newPlan.activate(LocalDate.now(clock));

    // Then close the previous active plan, if any
    previousActivePlan.ifPresent(
        previousPlan -> {
          previousPlan.close(LocalDate.now(clock).minusDays(1));
          repository.save(previousPlan);
        });

    repository.save(newPlan);

    return NutritionPlanMapper.toDetail(newPlan);
  }

  @Override
  public NutritionPlanDetailDTO findById(Long id) {
    NutritionPlan plan =
        repository.findById(id).orElseThrow(() -> new NutritionPlanNotFoundException(id));
    return NutritionPlanMapper.toDetail(plan);
  }

  @Override
  public List<NutritionPlanSummaryDTO> findByPatient(Long patientId) {
    return repository.findByPatientProfileIdOrderByStartDateDesc(patientId).stream()
        .map(NutritionPlanMapper::toSummary)
        .toList();
  }

  @Override
  public Optional<NutritionPlan> findActivePlan(Long patientId) {
    return repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
  }

  public Optional<NutritionPlan> findActivePlanByUserId(Long userId) {
    return repository.findByPatientProfileUserIdAndStatus(userId, PlanStatus.ACTIVE);
  }

  public boolean belongsToPatient(Long planId, Long patientId) {
    return repository
        .findById(planId)
        .map(plan -> plan.getPatientProfile().getUser().getId().equals(patientId))
        .orElse(false);
  }

  @Override
  public NutritionPlanDetailDTO findActiveByPatient(Long patientId) {
    NutritionPlan plan =
        findActivePlan(patientId)
            .orElseThrow(
                () ->
                    new NutritionPlanNotFoundException(
                        "No active nutrition plan found for patientId: " + patientId));
    return NutritionPlanMapper.toDetail(plan);
  }
}
