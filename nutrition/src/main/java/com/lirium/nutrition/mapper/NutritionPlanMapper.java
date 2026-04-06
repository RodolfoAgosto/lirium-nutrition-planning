package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import org.springframework.stereotype.Component;

@Component
public class NutritionPlanMapper {

    public NutritionPlanMapper() {

    }

    public static NutritionPlanDetailDTO toDetail(NutritionPlan plan) {
        return new NutritionPlanDetailDTO(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getStatus(),
                plan.getTargetGoal(),
                plan.getDailyCalories(),
                plan.getProteinGrams(),
                plan.getCarbGrams(),
                plan.getFatGrams(),
                plan.getWeek().stream()
                        .map(NutritionPlanMapper::toDailyPlanDetail)
                        .toList()
        );
    }

    private static DailyPlanDetailDTO toDailyPlanDetail(DailyPlan dailyPlan) {
        return new DailyPlanDetailDTO(
                dailyPlan.getDayOfWeek(),
                dailyPlan.getMeals().stream()
                        .map(NutritionPlanMapper::toPlanMealDetail)
                        .toList()
        );
    }

    private static PlanMealDetailDTO toPlanMealDetail(PlanMeal meal) {
        return new PlanMealDetailDTO(
                meal.getType(),
                meal.getFoodPortions().stream()
                        .map(NutritionPlanMapper::toPlanFoodPortionDetail)
                        .toList()
        );
    }

    private static PlanFoodPortionDetailDTO toPlanFoodPortionDetail(PlanFoodPortion portion) {
        double grams = portion.getFood().toGrams(portion.getQuantity(), portion.getUnit());
        return new PlanFoodPortionDetailDTO(
                portion.getFood().getName(),
                portion.getQuantity(),
                portion.getUnit(),
                (int)(portion.getFood().getCaloriesPer100g() * grams / 100),
                (int)(portion.getFood().getProteinPer100g()  * grams / 100),
                (int)(portion.getFood().getCarbsPer100g()    * grams / 100),
                (int)(portion.getFood().getFatPer100g()      * grams / 100)
        );
    }

    public static NutritionPlanSummaryDTO toSummary(NutritionPlan plan) {
        return new NutritionPlanSummaryDTO(
                plan.getId(),
                plan.getName(),
                plan.getStatus(),
                plan.getTargetGoal(),
                plan.getDailyCalories(),
                plan.getStartDate(),
                plan.getEndDate()
        );
    }

}