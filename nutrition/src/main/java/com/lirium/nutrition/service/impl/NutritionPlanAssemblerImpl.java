package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.NutritionPlanAssembler;
import com.lirium.nutrition.service.PlanMealAssembler;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Set;

@Service
public class NutritionPlanAssemblerImpl implements NutritionPlanAssembler {

    PlanMealAssembler planMealAssembler;

    public NutritionPlanAssemblerImpl(PlanMealAssembler planMealAssembler) {
        this.planMealAssembler = planMealAssembler;
    }

    @Override
    public NutritionPlan assemble(PatientProfile patient, Calories calories, MacroDistribution macros) {
        return assemble(patient, calories, macros, Collections.emptySet());
    }

    @Override
    public NutritionPlan assemble(PatientProfile patient, Calories calories,
                                  MacroDistribution macros, Set<FoodTag> additionalExcludedTags) {
        NutritionPlan nutritionPlan = NutritionPlan.generate(
                patient.getPrimaryGoal(),
                calories.amount(),
                macros.proteinGrams(),
                macros.carbGrams(),
                macros.fatGrams(),
                patient);

        for (DayOfWeek day : DayOfWeek.values()) {
            DailyPlan dailyPlan = DailyPlan.of(day, nutritionPlan);
            planMealAssembler.assemble(dailyPlan, patient, calories, macros, additionalExcludedTags);
            nutritionPlan.addDailyPlan(dailyPlan);
        }

        return nutritionPlan;
    }
}