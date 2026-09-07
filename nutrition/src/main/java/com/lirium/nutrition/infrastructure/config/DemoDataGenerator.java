package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(10)
@RequiredArgsConstructor
public class DemoDataGenerator implements CommandLineRunner {

  private final NutritionPlanService nutritionPlanService;
  private final DemoResetService demoResetService;

  @Override
  public void run(String... args) throws Exception {

    if (nutritionPlanService.findByPatient(1L).isEmpty()
        || nutritionPlanService.findByPatient(2L).isEmpty()
        || nutritionPlanService.findByPatient(3L).isEmpty()) {
      demoResetService.seedDemoData();
    }
  }
}
