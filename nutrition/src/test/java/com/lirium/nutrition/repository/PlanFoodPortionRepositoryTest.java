package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class PlanFoodPortionRepositoryTest {

    @Autowired
    private PlanFoodPortionRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByMeal() {

        PlanMeal breakfast = createMeal(MealType.BREAKFAST);

        Food chicken = createFood("Chicken");
        Food rice = createFood("Rice");

        PlanFoodPortion expected =
                createPortion(breakfast, chicken);

        createPortion(
                createMeal(MealType.LUNCH),
                rice
        );

        em.flush();
        em.clear();

        List<PlanFoodPortion> result =
                repository.findByMeal(breakfast);

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldFindByMealId() {

        PlanMeal breakfast = createMeal(MealType.BREAKFAST);

        Food chicken = createFood("Chicken");

        PlanFoodPortion expected =
                createPortion(breakfast, chicken);

        em.flush();
        em.clear();

        List<PlanFoodPortion> result =
                repository.findByMealId(breakfast.getId());

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldFindByFood() {

        Food chicken = createFood("Chicken");

        PlanFoodPortion expected =
                createPortion(
                        createMeal(MealType.BREAKFAST),
                        chicken
                );

        createPortion(
                createMeal(MealType.LUNCH),
                createFood("Rice")
        );

        em.flush();
        em.clear();

        List<PlanFoodPortion> result =
                repository.findByFood(chicken);

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldFindByMealAndFood() {

        PlanMeal breakfast =
                createMeal(MealType.BREAKFAST);

        Food chicken =
                createFood("Chicken");

        PlanFoodPortion expected =
                createPortion(
                        breakfast,
                        chicken
                );

        createPortion(
                breakfast,
                createFood("Rice")
        );

        em.flush();
        em.clear();

        List<PlanFoodPortion> result =
                repository.findByMealAndFood(
                        breakfast,
                        chicken
                );

        assertThat(result)
                .containsExactly(expected);
    }

    private PlanMeal createMeal(MealType type) {

        User user = new User(
                UUID.randomUUID() + "@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        PatientProfile patient =
                user.getPatientProfile();

        NutritionPlan plan =
                NutritionPlan.generate(
                        GoalType.WEIGHT_MAINTENANCE,
                        2000,
                        150,
                        220,
                        70,
                        patient
                );

        em.persist(plan);

        DailyPlan daily =
                DailyPlan.of(
                        DayOfWeek.MONDAY,
                        plan
                );

        plan.addDailyPlan(daily);

        em.persist(daily);

        PlanMeal meal =
                PlanMeal.of(type, daily);

        daily.addMeal(meal);

        em.persist(meal);

        return meal;
    }

    private Food createFood(String name) {

        Food food = Food.of(
                name,
                100,
                20,
                10,
                5,
                FoodCategory.PROTEIN,
                Set.of(MealType.BREAKFAST)
        );

        em.persist(food);

        return food;
    }

    private PlanFoodPortion createPortion(
            PlanMeal meal,
            Food food
    ) {

        PlanFoodPortion portion =
                PlanFoodPortion.of(
                        meal,
                        food,
                        100.0,
                        MeasureUnit.GRAM
                );

        meal.addFoodPortion(portion);

        em.persist(portion);

        return portion;
    }
}