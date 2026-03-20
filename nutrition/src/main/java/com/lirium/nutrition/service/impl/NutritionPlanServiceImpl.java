package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.NutritionPlanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NutritionPlanServiceImpl{

        //implements NutritionPlanService {

    /*
    private final NutritionPlanRepository planRepository;
    private final PatientProfileRepository patientRepository;
    private final NutritionPlanTemplateRepository templateRepository;

    @Override
    @Transactional
    public NutritionPlan createFromTemplate(Long patientId, Long templateId) {

        PatientProfile patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Patient not found: " + patientId));

        NutritionPlanTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Template not found: " + templateId));

        //NutritionPlan plan = NutritionPlan.of(patient, template);
        //return planRepository.save(plan);
    }

    @Override
    @Transactional
    public NutritionPlan activatePlan(Long planId) {

        NutritionPlan plan = findById(planId);

        // Desactiva el plan activo anterior si existe

        planRepository
                .findByPatientIdAndStatus(
                        plan.getPatient().getId(), PlanStatus.ACTIVE)
                .ifPresent(active -> {
                    active.deactivate();
                    planRepository.save(active);
                });

        plan.activate();
        return planRepository.save(plan);
    }

    @Override
    public NutritionPlan findById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan not found: " + planId));
    }

    @Override
    public List<NutritionPlan> findByPatient(Long patientId) {
        return planRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    public Optional<NutritionPlan> findActivePlan(Long patientId) {
        return planRepository
                .findByPatientIdAndStatus(patientId, PlanStatus.ACTIVE);
    }

 */

}