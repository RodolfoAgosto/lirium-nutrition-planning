package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.PlanFoodPortionRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import com.lirium.nutrition.testdata.NutritionPlanTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.util.EnumSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanFoodPortionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PlanFoodPortionRepository planFoodPortionRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private PlanMealRepository planMealRepository;

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Autowired
    private NutritionPlanTestDataFactory nutritionPlanTestDataFactory;

    private String adminToken;

    private User patient;

    private PlanMeal meal;

    private PlanFoodPortion portion;

    @BeforeEach
    void setup() {

        planFoodPortionRepository.deleteAll();
        planMealRepository.deleteAll();
        nutritionPlanRepository.deleteAll();

        patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient",
                "Test",
                Role.PATIENT
        ));

        User admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin",
                "Test",
                Role.ADMIN
        ));

        adminToken = "Bearer " + jwtService.generateToken(admin);


        NutritionPlan plan =
                nutritionPlanTestDataFactory.createDraftPlan(
                        patient.getPatientProfile()
                );

        plan = nutritionPlanRepository.saveAndFlush(plan);


        DailyPlan dailyPlan = DailyPlan.of(
                DayOfWeek.MONDAY,
                plan
        );

        dailyPlan = dailyPlanRepository.saveAndFlush(dailyPlan);


        meal = PlanMeal.of(
                MealType.LUNCH,
                dailyPlan
        );

        meal = planMealRepository.saveAndFlush(meal);


        Food chicken = foodRepository.save(
                Food.of(
                        "Chicken",
                        165,
                        31,
                        0,
                        4,
                        FoodCategory.PROTEIN,
                        EnumSet.allOf(MealType.class)
                )
        );


        portion = PlanFoodPortion.of(
                meal,
                chicken,
                200D,
                MeasureUnit.GRAM
        );

        planFoodPortionRepository.saveAndFlush(portion);
    }



    @Test
    @DisplayName("Debe obtener porciones de una comida")
    void shouldGetByMeal() throws Exception {

        System.out.println("MEAL ID TEST = " + meal.getId());

        mockMvc.perform(get("/api/plan-food-portions/meal/{id}", meal.getId())
                        .header("Authorization", adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


    @Test
    @DisplayName("Debe obtener una porción por id")
    void shouldGetById() throws Exception {

        mockMvc.perform(get("/api/plan-food-portions/{id}", portion.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(portion.getId()));
    }


    @Test
    @DisplayName("Debe retornar 404 cuando la porción no existe")
    void shouldReturnNotFoundWhenPortionDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/plan-food-portions/{id}", 999999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Debe retornar lista vacía cuando la comida no tiene porciones")
    void shouldReturnEmptyListWhenMealHasNoPortions() throws Exception {

        mockMvc.perform(get("/api/plan-food-portions/meal/{id}", 999999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }
}