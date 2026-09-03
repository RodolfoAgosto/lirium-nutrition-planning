package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequestDTO;
import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.service.DailyRecordService;
import com.lirium.nutrition.service.NutritionPlanGenerator;
import com.lirium.nutrition.service.NutritionPlanService;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
@Order(10)
@RequiredArgsConstructor
public class DemoDataGenerator implements CommandLineRunner {

  private final NutritionPlanService nutritionPlanService;
  private final DailyRecordService dailyRecordService;
  private final NutritionPlanGenerator nutritionPlanGenerator;
  private final NutritionPlanRepository nutritionPlanRepository;

  private final Clock clock;

  @Override
  public void run(String... args) throws Exception {

    if (nutritionPlanService.findByPatient(1L).isEmpty()) {
      seedAna();
    }
    if (nutritionPlanService.findByPatient(2L).isEmpty()) {
      seedJuan();
    }
    if (nutritionPlanService.findByPatient(3L).isEmpty()) {
      seedMaria();
    }
  }

  private void seedAna() {

    // ACTIVE
    Long activeId = nutritionPlanGenerator.generate(1L).id();
    nutritionPlanService.activatePlan(activeId);

    // DRAFT: generado, sin activar -- muestra ese estado real
    nutritionPlanGenerator.generate(1L);
  }

  private void seedJuan() {

    // Genero el plan
    Long planId = nutritionPlanGenerator.generate(2L).id();
    NutritionPlan juanPlan =
        nutritionPlanRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", planId));

    // Fuerzo la fecha de activacion a pasado
    LocalDate activationDate = LocalDate.now(clock).minusDays(30);
    juanPlan.activate(activationDate);
    nutritionPlanService.activatePlan(planId);
    nutritionPlanRepository.save(juanPlan);

    // 5 dias con valores default
    for (int i = 15; i <= 19; i++) {
      var day = dailyRecordService.getOrCreateForDate(2L, activationDate.plusDays(i));
    }

    // 7 días sumando un alimento
    for (int i = 20; i <= 26; i++) {
      var day = dailyRecordService.getOrCreateForDate(2L, activationDate.plusDays(i));
      var meal = day.meals().get(0);
      dailyRecordService.addPortion(
          meal.id(), new FoodPortionAddRequestDTO(1L, 150.0, MeasureUnit.GRAM));
    }

    // 1 dia cambiando un alimento
    var day27 = dailyRecordService.getOrCreateForDate(2L, activationDate.plusDays(27));
    var firstMeal = day27.meals().get(0);
    dailyRecordService.removePortion(day27.id(), firstMeal.id(), firstMeal.portions().get(0).id());
    dailyRecordService.addPortion(
        firstMeal.id(), new FoodPortionAddRequestDTO(24L, 1.0, MeasureUnit.UNIT)); // Banana
  }

  private void seedMaria() {
    // 'WEIGHT_MAINTENANCE', 'FEMALE'
    Long planId = nutritionPlanGenerator.generate(3L).id();
    NutritionPlan mariaPlan =
        nutritionPlanRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", planId));

    LocalDate activationDate = LocalDate.now(clock).minusDays(60);
    mariaPlan.activate(activationDate);
    nutritionPlanService.activatePlan(planId);
    nutritionPlanRepository.save(mariaPlan);

    CompleteNutritionPlanRequestDTO completeNutritionPlanRequestDTO =
        new CompleteNutritionPlanRequestDTO(
            "Phase 1: Weight Maintenance",
            "Patient maintained a stable body weight with good adherence to a balanced nutrition plan and adequate nutritional intake.");
    nutritionPlanService.complete(planId, completeNutritionPlanRequestDTO);
  }
}
