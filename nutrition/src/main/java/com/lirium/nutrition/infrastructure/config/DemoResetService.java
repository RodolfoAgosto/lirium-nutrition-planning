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
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("demo")
@RequiredArgsConstructor
public class DemoResetService {

  private final JdbcTemplate jdbc;

  private final NutritionPlanService nutritionPlanService;
  private final DailyRecordService dailyRecordService;
  private final NutritionPlanGenerator nutritionPlanGenerator;
  private final NutritionPlanRepository nutritionPlanRepository;

  private final Clock clock;

  @Scheduled(
      cron = "0 0 4 * * *",
      // cron = "0 */25 * * * *",
      zone = "America/Argentina/Tucuman")
  public void scheduledResetDemoState() {
    resetDemoState();
  }

  public void resetDemoState() {
    jdbc.execute(
        """
            TRUNCATE
                nutrition_plans,
                daily_records,
                meal_records,
                food_portion_records
            CASCADE
            """);
    seedDemoData();
  }

  public void seedDemoData() {
    LocalDate demoDate = LocalDate.now(clock);
    seedAna();
    seedJuan(demoDate);
    seedMaria(demoDate);
  }

  private void seedAna() {

    // ACTIVE
    Long activeId = nutritionPlanGenerator.generate(1L).id();
    nutritionPlanService.activatePlan(activeId);

    // DRAFT: generado, sin activar -- muestra ese estado real
    nutritionPlanGenerator.generate(1L);
  }

  private void seedJuan(LocalDate demoDate) {

    // Genero el plan
    Long planId = nutritionPlanGenerator.generate(2L).id();
    NutritionPlan juanPlan =
        nutritionPlanRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", planId));

    // Activate through the business service to apply all activation rules.
    // Then override the activation date to simulate historical demo data.
    LocalDate activationDate = demoDate.minusDays(30);
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

  private void seedMaria(LocalDate demoDate) {
    // 'WEIGHT_MAINTENANCE', 'FEMALE'
    Long planId = nutritionPlanGenerator.generate(3L).id();
    NutritionPlan mariaPlan =
        nutritionPlanRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan", planId));

    LocalDate activationDate = demoDate.minusDays(60);
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
