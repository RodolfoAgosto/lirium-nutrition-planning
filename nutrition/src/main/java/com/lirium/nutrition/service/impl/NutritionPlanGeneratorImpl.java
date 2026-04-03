package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.NutritionPlanMapper;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.CalorieCalculator;
import com.lirium.nutrition.service.MacroDistributor;
import com.lirium.nutrition.service.NutritionPlanAssembler;
import com.lirium.nutrition.service.NutritionPlanGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanGeneratorImpl implements NutritionPlanGenerator {

    private final CalorieCalculator calorieCalculator;
    private final MacroDistributor macroDistributor;
    private final PatientProfileRepository repository;
    private final NutritionPlanAssembler nutritionPlanAssembler;
    private final NutritionPlanRepository nutritionPlanRepository;

    @Transactional
    public NutritionPlanDetailDTO generate(Long patientId) {

            PatientProfile patient = repository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

           if (nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT) ||
               nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE)) {
              throw new IllegalStateException("Patient already has an active or draft plan");
           }

            Calories calories = calorieCalculator.calculate(patient);

            MacroDistribution macros = macroDistributor.distribute(patient, calories);

            NutritionPlan plan = nutritionPlanAssembler.assemble(patient, calories, macros);

            nutritionPlanRepository.save(plan);

            return NutritionPlanMapper.toDetail(plan);

    }

    @Override
    public NutritionPlanDetailDTO generateFromTemplate(Long userId, Long templateId) {
        return null;
    }

}
