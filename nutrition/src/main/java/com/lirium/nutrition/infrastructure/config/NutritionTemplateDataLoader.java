package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(4)
public class NutritionTemplateDataLoader implements CommandLineRunner {

    private final NutritionPlanTemplateRepository templateRepository;

    @Override
    public void run(String... args) {

        if (templateRepository.count() > 0) return;

        templateRepository.saveAll(List.of(

                NutritionPlanTemplate.of(
                        "Weight Loss - Standard",
                        "High protein, moderate carbs, low fat plan for gradual weight loss.",
                        GoalType.WEIGHT_LOSS,
                        40, 35, 25,
                        Set.of()
                ),

                NutritionPlanTemplate.of(
                        "Muscle Gain - High Protein",
                        "Very high protein intake with complex carbs to support muscle hypertrophy.",
                        GoalType.MUSCLE_GAIN,
                        45, 40, 15,
                        Set.of()
                ),

                NutritionPlanTemplate.of(
                        "Vegan - Weight Maintenance",
                        "Plant-based balanced plan excluding all animal products.",
                        GoalType.WEIGHT_MAINTENANCE,
                        25, 55, 20,
                        Set.of(
                                FoodTag.MEAT, FoodTag.FISH, FoodTag.EGG,
                                FoodTag.LACTOSE, FoodTag.HONEY, FoodTag.GELATIN
                        )
                ),

                NutritionPlanTemplate.of(
                        "Metabolic Health - Gluten & Lactose Free",
                        "Anti-inflammatory plan free of gluten and lactose.",
                        GoalType.METABOLIC_HEALTH,
                        30, 40, 30,
                        Set.of(FoodTag.GLUTEN, FoodTag.LACTOSE)
                ),

                NutritionPlanTemplate.of(
                        "Pregnancy Health - Balanced",
                        "Nutrient-dense plan for pregnancy. Avoids alcohol and raw fish.",
                        GoalType.PREGNANCY_HEALTH,
                        25, 50, 25,
                        Set.of(FoodTag.ALCOHOL, FoodTag.FISH)
                )
        ));
    }
}