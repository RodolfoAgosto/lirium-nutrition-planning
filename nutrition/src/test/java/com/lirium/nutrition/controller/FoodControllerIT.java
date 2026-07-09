package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.testdata.NutritionPlanTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.DayOfWeek;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FoodControllerIT extends AbstractIntegrationTest{

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private NutritionPlanTestDataFactory nutritionPlanTestDataFactory;

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private User admin;
    private User nutritionist;
    private User patient;

    private Food chicken;
    private Food rice;
    private Food apple;


    @BeforeEach
    void setup() {

        nutritionPlanRepository.deleteAll();
        foodRepository.deleteAll();

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin",
                "Test",
                Role.ADMIN));

        nutritionist = userRepository.save(new User(
                "nutritionist@test.com",
                passwordEncoder.encode("1234"),
                "Nutritionist",
                "Test",
                Role.NUTRITIONIST));

        patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient",
                "Test",
                Role.PATIENT));

        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);

        chicken = foodRepository.save(
                Food.of(
                        "Chicken Breast",
                        165,
                        31,
                        0,
                        4,
                        FoodCategory.PROTEIN,
                        EnumSet.allOf(MealType.class)
                )
        );

        rice = foodRepository.save(
                Food.of(
                        "Rice",
                        130,
                        3,
                        28,
                        1,
                        FoodCategory.CARB,
                        EnumSet.allOf(MealType.class)
                )
        );

        apple = foodRepository.save(
                Food.of(
                        "Apple",
                        52,
                        0,
                        14,
                        0,
                        FoodCategory.FRUIT,
                        EnumSet.allOf(MealType.class)
                )
        );
    }

    @Test
    @DisplayName("ADMIN puede eliminar un alimento")
    void shouldDeleteFood() throws Exception {

        mockMvc.perform(delete("/api/foods/{id}", apple.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        assertFalse(foodRepository.findById(apple.getId()).isPresent());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el alimento no existe")
    void shouldReturnNotFoundWhenDeletingUnknownFood() throws Exception {

        mockMvc.perform(delete("/api/foods/{id}", 999999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 409 cuando el alimento está siendo utilizado")
    void shouldReturnConflictWhenDeletingFoodInUse() throws Exception {

        NutritionPlan plan =
                nutritionPlanTestDataFactory.createDraftPlan(
                        patient.getPatientProfile()
                );

        DailyPlan dailyPlan = DailyPlan.of(
                DayOfWeek.MONDAY,
                plan
        );

        PlanMeal meal = PlanMeal.of(
                MealType.LUNCH,
                dailyPlan
        );

        PlanFoodPortion portion = PlanFoodPortion.of(
                meal,
                chicken,
                200.0,
                MeasureUnit.GRAM
        );

        meal.addFoodPortion(portion);
        dailyPlan.addMeal(meal);
        plan.addDailyPlan(dailyPlan);

        nutritionPlanRepository.saveAndFlush(plan);

        mockMvc.perform(delete("/api/foods/{id}", chicken.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ADMIN puede crear un alimento")
    void shouldCreateFood() throws Exception {

        String body = """
    {
        "name": "Banana",
        "caloriesPer100g": 89,
        "proteinPer100g": 1.1,
        "carbsPer100g": 23,
        "fatPer100g": 0.3,
        "category": "FRUIT",
        "suitableFor": ["BREAKFAST"]
    }
    """;

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("Debe obtener un alimento por id")
    void shouldFindFoodById() throws Exception {

        mockMvc.perform(get("/api/foods/{id}", chicken.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe listar alimentos")
    void shouldFindAllFoods() throws Exception {

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN puede actualizar alimento")
    void shouldUpdateFood() throws Exception {

        String body = """
    {
        "name":"Chicken Updated",
        "caloriesPer100g":200,
        "proteinPer100g":35,
        "carbsPer100g":0,
        "fatPer100g":5
    }
    """;

        mockMvc.perform(put("/api/foods/{id}", chicken.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Food updated = foodRepository.findById(chicken.getId()).orElseThrow();

        assertEquals("Chicken Updated", updated.getName());
        assertEquals(200, updated.getCaloriesPer100g());
    }


}