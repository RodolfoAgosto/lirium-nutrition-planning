package com.lirium.nutrition.testdata;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.h2.table.Plan;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class NutritionPlanTestDataFactory {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final FoodRepository foodRepository;

    /* =========================
       PUBLIC API
    ========================== */

    @Transactional
    public NutritionPlan createDraftPlan(PatientProfile patient) {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_MAINTENANCE,
                2000,
                120,
                200,
                70,
                patient
        );

        plan.completeBasic("Test Plan", "Integration test plan");

        buildWeek(plan);

        return nutritionPlanRepository.save(plan);
    }

    @Transactional
    public NutritionPlan createActivePlan(PatientProfile patient) {

        NutritionPlan plan = createDraftPlan(patient);
        plan.activate(java.time.LocalDate.now());

        return nutritionPlanRepository.save(plan);
    }

    @Transactional
    public NutritionPlan createInactivePlan(PatientProfile patient) {

        NutritionPlan plan = createActivePlan(patient);
        plan.close(java.time.LocalDate.now());

        return nutritionPlanRepository.save(plan);
    }

    /* === INTERNAL BUILDERS === */

    private void buildWeek(NutritionPlan plan) {

        for (DayOfWeek day : DayOfWeek.values()) {

            DailyPlan daily = DailyPlan.of(day, plan);
            plan.addDailyPlan(daily);

            createMeals(daily);
        }
    }

    private void createMeals(DailyPlan dailyPlan) {

        for (MealType type : MealType.values()) {

            PlanMeal meal = PlanMeal.of(type, dailyPlan);
            dailyPlan.addMeal(meal);

            PlanFoodPortion foodPortion = PlanFoodPortion.of(
                    meal,
                    chicken(),
                    100.0,
                    MeasureUnit.GRAM
            );

            meal.addFoodPortion(foodPortion);
        }
    }

    private Food chicken() {
        return foodRepository.findByName("Chicken Breast")
                .orElseGet(() -> foodRepository.save(
                        Food.of(
                                "Chicken Breast",
                                165,
                                31,
                                0,
                                3,
                                FoodCategory.PROTEIN,
                                EnumSet.allOf(MealType.class)
                        )
                ));
    }
}
