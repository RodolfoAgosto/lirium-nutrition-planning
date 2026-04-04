package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.service.PlanFoodPortionAssembler;
import com.lirium.nutrition.service.PlanMealAssembler;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class PlanMealAssemblerImpl implements PlanMealAssembler {

    private final PlanFoodPortionAssembler planFoodPortionAssembler;

    public PlanMealAssemblerImpl(PlanFoodPortionAssembler planFoodPortionAssembler) {
        this.planFoodPortionAssembler = planFoodPortionAssembler;
    }

    @Override
    public void assemble(DailyPlan dailyPlan, PatientProfile patient,
                         Calories calories, MacroDistribution macros) {
        assemble(dailyPlan, patient, calories, macros, Collections.emptySet());
    }

    @Override
    public void assemble(DailyPlan dailyPlan, PatientProfile patient, Calories calories, MacroDistribution macros, Set<FoodTag> additionalExcludedTags) {
        for (MealType meal : MealType.values()) {
            PlanMeal planMeal = PlanMeal.of(meal, dailyPlan);
            Calories mealCalories = new Calories((int)(calories.amount() * meal.getCalorieRatio()));
            Fat fat       = new Fat((int)(macros.fatGrams()     * meal.getFatRatio()));
            Carbs carbs   = new Carbs((int)(macros.carbGrams()  * meal.getCarbRatio()));
            Protein protein = new Protein((int)(macros.proteinGrams() * meal.getProteinRatio()));
            planFoodPortionAssembler.assemble(planMeal, patient, additionalExcludedTags,
                    mealCalories, fat, carbs, protein);
            dailyPlan.addMeal(planMeal);
        }
    }

}
