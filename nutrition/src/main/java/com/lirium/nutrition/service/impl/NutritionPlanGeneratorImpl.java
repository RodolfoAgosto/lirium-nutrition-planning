package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.exception.NutritionPlanTemplateNotFoundException;
import com.lirium.nutrition.exception.PatientProfileNotFoundException;
import com.lirium.nutrition.exception.PlanConflictException;
import com.lirium.nutrition.exception.UnprocessableEntityException;
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
import java.util.ArrayList;
import java.util.List;
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

    PatientProfile patient =
        repository
            .findById(patientId)
            .orElseThrow(
                () -> {
                  log.warn("Patient not found id={}", patientId);
                  return new PatientProfileNotFoundException(patientId);
                });

    ensureNoDraftPlan(patientId);

    ensureProfileIsComplete(patient);

    Calories calories = calorieCalculator.calculate(patient);

    MacroDistribution macros = macroDistributor.distribute(patient, calories);

    log.debug(
        "Calculated values patientId={} calories={} protein={} carbs={} fat={}",
        patientId,
        calories.amount(),
        macros.proteinGrams(),
        macros.carbGrams(),
        macros.fatGrams());

    NutritionPlan plan = nutritionPlanAssembler.assemble(patient, calories, macros);

    nutritionPlanRepository.save(plan);

    log.info(
        "Nutrition plan generated successfully patientId={} planId={}", patientId, plan.getId());

    return NutritionPlanMapper.toDetail(plan);
  }

  @Override
  @Transactional
  public NutritionPlanDetailDTO generateFromTemplate(Long patientId, Long templateId) {

    log.info(
        "Generating nutrition plan from template patientId={} templateId={}",
        patientId,
        templateId);

    PatientProfile patient =
        repository
            .findById(patientId)
            .orElseThrow(
                () -> {
                  log.warn("Patient not found id={}", patientId);
                  return new PatientProfileNotFoundException(patientId);
                });

    ensureNoDraftPlan(patientId);

    NutritionPlanTemplate template =
        templateRepository
            .findById(templateId)
            .orElseThrow(
                () -> {
                  log.warn("Template not found id={}", templateId);
                  return new NutritionPlanTemplateNotFoundException(templateId);
                });

    ensureProfileIsComplete(patient);

    Calories calories = calorieCalculator.calculate(patient);

    MacroDistribution macros = macroDistributor.distributeFromTemplate(calories, template);

    if (log.isDebugEnabled()) {
      log.debug(
          "Template values patientId={} templateId={} calories={} protein={} carbs={} fat={}",
          patientId,
          templateId,
          calories.amount(),
          macros.proteinGrams(),
          macros.carbGrams(),
          macros.fatGrams());
    }

    NutritionPlan plan =
        nutritionPlanAssembler.assemble(patient, calories, macros, template.getExcludedTags());

    plan.rename(template.getName());

    nutritionPlanRepository.save(plan);

    log.info(
        "Nutrition plan generated from template successfully patientId={} planId={} templateId={}",
        patientId,
        plan.getId(),
        templateId);

    return NutritionPlanMapper.toDetail(plan);
  }

  /**
   * A patient can have at most one DRAFT plan. An ACTIVE plan does not block generation: the new
   * draft replaces it when activated.
   */
  private void ensureNoDraftPlan(Long patientId) {
    if (nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT)) {
      log.warn("Plan generation failed - draft already exists patientId={}", patientId);
      throw new PlanConflictException("Patient already has a draft plan");
    }
  }

  /**
   * The energy requirement uses sex, weight, height, age (from the birth date), activity level and
   * primary goal: all of them must be present before generating a plan.
   */
  private void ensureProfileIsComplete(PatientProfile patient) {
    List<String> missing = new ArrayList<>();

    if (patient.getSex() == null) missing.add("sex");
    if (patient.getUser().getBirthDate() == null) missing.add("birth date");
    if (patient.getWeight() == null) missing.add("weight");
    if (patient.getHeight() == null) missing.add("height");
    if (patient.getActivityLevel() == null) missing.add("activity level");
    if (patient.getPrimaryGoal() == null) missing.add("primary goal");

    if (!missing.isEmpty()) {
      throw new UnprocessableEntityException(
          "Patient profile is incomplete. Missing: " + String.join(", ", missing));
    }
  }
}
