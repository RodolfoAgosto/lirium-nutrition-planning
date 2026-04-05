package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequest;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
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
    private final NutritionPlanMapper mapper;

    @Transactional
    public NutritionPlanDetailDTO complete(Long id, CompleteNutritionPlanRequest request) {

        NutritionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NutritionPlan not found"));

        plan.completeBasic(request.getName(), request.getDescription());

        return mapper.toDetail(plan);
    }

    @Override
    public NutritionPlan createFromTemplate(Long patientId, Long templateId) {
        return null;
    }

    @Override
    @Transactional
    public NutritionPlanDetailDTO activatePlan(Long planId){

        NutritionPlan plan = repository.findById(planId)
                .orElseThrow(() -> new RuntimeException("NutritionPlan not found"));

        plan.activate(LocalDate.now());

        return mapper.toDetail(plan);
    }


    @Override
    public NutritionPlanDetailDTO findById(Long id) {
        NutritionPlan plan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", id));
        return NutritionPlanMapper.toDetail(plan);
    }

    @Override
    public List<NutritionPlan> findByPatient(Long patientId) {
        return null;
    }

    @Override
    public Optional<NutritionPlan> findActivePlan(Long patientId) {
        return repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
    }


}