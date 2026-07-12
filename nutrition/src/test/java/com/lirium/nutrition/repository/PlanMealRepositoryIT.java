package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class PlanMealRepositoryIT {

    @Autowired
    private PlanMealRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindMealsByDailyPlan() {

        DailyPlan monday = createDailyPlan();
        DailyPlan tuesday = createDailyPlan();

        PlanMeal breakfast = createMeal(monday, MealType.BREAKFAST);
        PlanMeal lunch = createMeal(monday, MealType.LUNCH);

        createMeal(tuesday, MealType.BREAKFAST);

        em.flush();
        em.clear();

        List<PlanMeal> result =
                repository.findByDailyPlan(monday);

        assertThat(result)
                .containsExactlyInAnyOrder(breakfast, lunch);
    }

    @Test
    void shouldFindMealsByDailyPlanId() {

        DailyPlan dailyPlan = createDailyPlan();

        PlanMeal breakfast =
                createMeal(dailyPlan, MealType.BREAKFAST);

        PlanMeal lunch =
                createMeal(dailyPlan, MealType.LUNCH);

        em.flush();
        em.clear();

        List<PlanMeal> result =
                repository.findByDailyPlanId(dailyPlan.getId());

        assertThat(result)
                .containsExactlyInAnyOrder(breakfast, lunch);
    }

    @Test
    void shouldFindMealsByDailyPlanAndType() {

        DailyPlan dailyPlan = createDailyPlan();

        PlanMeal expected =
                createMeal(dailyPlan, MealType.LUNCH);

        createMeal(dailyPlan, MealType.BREAKFAST);

        em.flush();
        em.clear();

        List<PlanMeal> result =
                repository.findByDailyPlanAndType(
                        dailyPlan,
                        MealType.LUNCH
                );

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldReturnEmptyListWhenNoMealsMatch() {

        DailyPlan dailyPlan = createDailyPlan();

        createMeal(dailyPlan, MealType.BREAKFAST);

        em.flush();
        em.clear();

        List<PlanMeal> result =
                repository.findByDailyPlanAndType(
                        dailyPlan,
                        MealType.DINNER
                );

        assertThat(result).isEmpty();
    }

    // =========================
    // Helpers
    // =========================

    private DailyPlan createDailyPlan() {

        User user = new User(
                UUID.randomUUID() + "@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        PatientProfile patient = user.getPatientProfile();

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_MAINTENANCE,
                2000,
                150,
                220,
                70,
                patient
        );

        em.persist(plan);

        DailyPlan dailyPlan =
                DailyPlan.of(
                        DayOfWeek.MONDAY,
                        plan
                );

        plan.addDailyPlan(dailyPlan);

        em.persist(dailyPlan);

        return dailyPlan;
    }

    private PlanMeal createMeal(
            DailyPlan dailyPlan,
            MealType type
    ) {

        PlanMeal meal =
                PlanMeal.of(type, dailyPlan);

        dailyPlan.addMeal(meal);

        em.persist(meal);

        return meal;
    }

}