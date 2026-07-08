package com.lirium.nutrition.testdata;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class NutritionPlanTemplateTestDataFactory {

    private final NutritionPlanTemplateRepository nutritionPlanTemplateRepository;

    @Transactional
    public NutritionPlanTemplate createTemplate() {

        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss Template",
                        "Integration test template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        EnumSet.noneOf(FoodTag.class)
                );

        return nutritionPlanTemplateRepository.save(template);
    }

    @Transactional
    public NutritionPlanTemplate createTemplate(String name) {

        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        name,
                        "Integration test template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        EnumSet.noneOf(FoodTag.class)
                );

        return nutritionPlanTemplateRepository.save(template);
    }
}