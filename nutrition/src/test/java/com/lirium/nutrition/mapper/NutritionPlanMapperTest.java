package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NutritionPlanMapperTest {

    @Test
    void shouldMapNutritionPlanToDetail() {

        User user = new User(
                "patient@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                180,
                60,
                patient
        );

        plan.completeBasic(
                "Weight Loss",
                "Description"
        );

        Food chicken = Food.of(
                "Chicken",
                200,
                30,
                0,
                5,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH)
        );

        DailyPlan monday = DailyPlan.of(
                DayOfWeek.MONDAY,
                plan
        );

        PlanMeal lunch = PlanMeal.of(
                MealType.LUNCH,
                monday
        );

        PlanFoodPortion portion = PlanFoodPortion.of(
                lunch,
                chicken,
                150.0,
                MeasureUnit.GRAM
        );

        lunch.addFoodPortion(portion);
        monday.addMeal(lunch);
        plan.addDailyPlan(monday);

        NutritionPlanDetailDTO dto =
                NutritionPlanMapper.toDetail(plan);

        assertThat(dto.name()).isEqualTo("Weight Loss");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.targetGoal()).isEqualTo(GoalType.WEIGHT_LOSS);
        assertThat(dto.dailyCalories()).isEqualTo(2000);

        assertThat(dto.week()).hasSize(1);

        DailyPlanDetailDTO day = dto.week().getFirst();
        assertThat(day.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        assertThat(day.meals()).hasSize(1);

        PlanMealDetailDTO meal = day.meals().getFirst();
        assertThat(meal.type()).isEqualTo(MealType.LUNCH);

        assertThat(meal.portions()).hasSize(1);

        PlanFoodPortionDetailDTO food = meal.portions().getFirst();

        assertThat(food.foodName()).isEqualTo("Chicken");
        assertThat(food.quantity()).isEqualTo(150.0);
        assertThat(food.unit()).isEqualTo(MeasureUnit.GRAM);
        assertThat(food.calories()).isEqualTo(300); // 200 * 150 / 100
        assertThat(food.protein()).isEqualTo(45);   // 30 * 150 / 100
        assertThat(food.carbs()).isEqualTo(0);
        assertThat(food.fat()).isEqualTo(7);        // 5 * 150 / 100
    }

    @Test
    void shouldMapNutritionPlanToSummary() {

        User user = new User(
                "patient@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_MAINTENANCE,
                2200,
                160,
                250,
                70,
                patient
        );

        plan.completeBasic(
                "Maintenance",
                "Description"
        );

        NutritionPlanSummaryDTO dto =
                NutritionPlanMapper.toSummary(plan);

        assertThat(dto.name()).isEqualTo("Maintenance");
        assertThat(dto.status()).isEqualTo(PlanStatus.DRAFT);
        assertThat(dto.targetGoal()).isEqualTo(GoalType.WEIGHT_MAINTENANCE);
        assertThat(dto.dailyCalories()).isEqualTo(2200);
        assertThat(dto.startDate()).isNull();
        assertThat(dto.endDate()).isNull();
    }

}