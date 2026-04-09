package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.NutritionPlanMapper;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.CalorieCalculator;
import com.lirium.nutrition.service.MacroDistributor;
import com.lirium.nutrition.service.NutritionPlanAssembler;
import com.lirium.nutrition.service.NutritionPlanGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanGeneratorImpl implements NutritionPlanGenerator {

    private final CalorieCalculator calorieCalculator;
    private final MacroDistributor macroDistributor;
    private final PatientProfileRepository repository;
    private final NutritionPlanAssembler nutritionPlanAssembler;
    private final NutritionPlanRepository nutritionPlanRepository;
    private final NutritionPlanTemplateRepository templateRepository;

    @Transactional
    public NutritionPlanDetailDTO generate(Long patientId) {

        log.info("Generating nutrition plan patientId={}", patientId);

        PatientProfile patient = repository.findById(patientId)
                .orElseThrow(() -> {
                    log.warn("Patient not found id={}", patientId);
                    return new ResourceNotFoundException("Patient", patientId);
                });

        if (nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT)) {
            log.warn("Plan generation failed - draft already exists patientId={}", patientId);
            throw new IllegalStateException("Patient already has an active or draft plan");
        }

        Calories calories = calorieCalculator.calculate(patient);

        MacroDistribution macros = macroDistributor.distribute(patient, calories);

        if (log.isDebugEnabled()) {
            log.debug("Calculated values patientId={} calories={} protein={} carbs={} fat={}",
                    patientId,
                    calories.amount(),
                    macros.proteinGrams(),
                    macros.carbGrams(),
                    macros.fatGrams());
        }

        NutritionPlan plan = nutritionPlanAssembler.assemble(patient, calories, macros);

        nutritionPlanRepository.save(plan);

        log.info("Nutrition plan generated successfully patientId={} planId={}", patientId, plan.getId());

        return NutritionPlanMapper.toDetail(plan);

    }

    @Override
    @Transactional
    public NutritionPlanDetailDTO generateFromTemplate(Long patientId, Long templateId) {

        log.info("Generating nutrition plan from template patientId={} templateId={}", patientId, templateId);

        PatientProfile patient = repository.findById(patientId)
                .orElseThrow(() -> {
                    log.warn("Patient not found id={}", patientId);
                    return new ResourceNotFoundException("Patient", patientId);
                });

        if (nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT) ||
                nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE)) {
            log.warn("Template plan generation failed - existing plan found patientId={}", patientId);
            throw new IllegalStateException("Patient already has an active or draft plan");
        }

        NutritionPlanTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> {
                    log.warn("Template not found id={}", templateId);
                    return new ResourceNotFoundException("Template", templateId);
                });

        Calories calories = calorieCalculator.calculate(patient);

        MacroDistribution macros = macroDistributor.distributeFromTemplate(calories, template);

        if (log.isDebugEnabled()) {
            log.debug("Template values patientId={} templateId={} calories={} protein={} carbs={} fat={}",
                    patientId,
                    templateId,
                    calories.amount(),
                    macros.proteinGrams(),
                    macros.carbGrams(),
                    macros.fatGrams());
        }

        NutritionPlan plan = nutritionPlanAssembler.assemble(patient, calories, macros, template.getExcludedTags());

        nutritionPlanRepository.save(plan);

        log.info("Nutrition plan generated from template successfully patientId={} planId={} templateId={}", patientId, plan.getId(), templateId);

        return NutritionPlanMapper.toDetail(plan);
    }

}
