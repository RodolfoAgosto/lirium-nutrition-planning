package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequest;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.NutritionPlanMapper;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanServiceImpl implements NutritionPlanService {

    private final NutritionPlanRepository repository;

    @Transactional
    public NutritionPlanDetailDTO complete(Long id, CompleteNutritionPlanRequest request) {

        NutritionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NutritionPlan not found"));

        plan.completeBasic(request.getName(), request.getDescription());

        return NutritionPlanMapper.toDetail(plan);
    }

    @Override
    public NutritionPlan createFromTemplate(Long patientId, Long templateId) {
        return null;
    }

    @Override
    @Transactional
    public NutritionPlanDetailDTO activatePlan(Long planId) {

        NutritionPlan newPlan = repository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", planId));

        Long patientId = newPlan.getPatientProfile().getId();

        // Cierra el plan anterior si existe
        repository
                .findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE)
                .ifPresent(previousPlan -> {
                    previousPlan.close(LocalDate.now().minusDays(1));
                    repository.save(previousPlan);
                });

        // Activa el nuevo
        newPlan.activate(LocalDate.now());
        repository.save(newPlan);

        return NutritionPlanMapper.toDetail(newPlan);
    }


    @Override
    public NutritionPlanDetailDTO findById(Long id) {
        NutritionPlan plan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", id));
        return NutritionPlanMapper.toDetail(plan);
    }

    @Override
    public List<NutritionPlanSummaryDTO> findByPatient(Long patientId) {
        return repository
                .findByPatientProfileIdOrderByStartDateDesc(patientId)
                .stream()
                .map(NutritionPlanMapper::toSummary)
                .toList();
    }

    @Override
    public Optional<NutritionPlan> findActivePlan(Long patientId) {
        return repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
    }

}