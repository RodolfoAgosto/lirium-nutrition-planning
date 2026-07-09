package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateFoodRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
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
import org.springframework.http.MediaType;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanMealControllerIT extends AbstractIntegrationTest {

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private User admin;
    private User nutritionist;
    private User patient;

    private NutritionPlan nutritionPlan;
    private DailyPlan dailyPlan;
    private PlanMeal meal;
    private PlanFoodPortion portion;

    private Food chicken;
    private Food rice;

    private Food food2;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Autowired
    private PlanMealRepository planMealRepository;

    @Autowired
    private PlanFoodPortionRepository planFoodPortionRepository;

    @Autowired
    private NutritionPlanTestDataFactory nutritionPlanTestDataFactory;

    @BeforeEach
    void setup() {

        planFoodPortionRepository.deleteAll();
        planMealRepository.deleteAll();
        dailyPlanRepository.deleteAll();
        nutritionPlanRepository.deleteAll();

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

        nutritionPlan = nutritionPlanTestDataFactory.createDraftPlan(patient.getPatientProfile());

        dailyPlan = nutritionPlan.getWeek().getFirst();

        meal = dailyPlan.getMeals().getFirst();

        portion = meal.getFoodPortions().getFirst();

        chicken = portion.getFood();

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

        food2 = foodRepository.save(
                Food.of(
                        "Potato",
                        130,
                        3,
                        28,
                        1,
                        FoodCategory.CARB,
                        EnumSet.allOf(MealType.class)
                )
        );

    }

    @Test
    @DisplayName("ADMIN puede obtener un plan meal por id")
    void shouldReturnMealByIdWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        get("/api/plan-meals/" + meal.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(meal.getId()))
                .andExpect(jsonPath("$.type").value(meal.getType().name()))
                .andExpect(jsonPath("$.dailyPlanId").value(dailyPlan.getId()));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el plan meal no existe")
    void shouldReturnNotFoundWhenMealDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/plan-meals/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede obtener todas las comidas de un día")
    void shouldReturnMealsByDailyPlan() throws Exception {

        mockMvc.perform(
                        get("/api/plan-meals/day/" + dailyPlan.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el día no tiene comidas")
    void shouldReturnEmptyListWhenDailyPlanDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/plan-meals/day/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("ADMIN puede crear un plan meal")
    void shouldCreatePlanMeal() throws Exception {

        PlanMealCreateRequestDTO dto = new PlanMealCreateRequestDTO(
                MealType.BREAKFAST.name(),
                dailyPlan.getId(),
                List.of()
        );

        mockMvc.perform(
                        post("/api/plan-meals")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value(MealType.BREAKFAST.name()))
                .andExpect(jsonPath("$.dailyPlanId").value(dailyPlan.getId()));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el daily plan no existe")
    void shouldReturnNotFoundWhenCreatingMealForUnknownDailyPlan() throws Exception {

        PlanMealCreateRequestDTO dto = new PlanMealCreateRequestDTO(
                MealType.BREAKFAST.name(),
                999999L,
                List.of()
        );

        mockMvc.perform(
                        post("/api/plan-meals")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el tipo de comida es inválido")
    void shouldReturnBadRequestWhenMealTypeIsInvalid() throws Exception {

        String json = """
        {
            "type":"INVALID",
            "dailyPlanId":%d,
            "foodPortionIds":[]
        }
        """.formatted(dailyPlan.getId());

        mockMvc.perform(
                        post("/api/plan-meals")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN puede eliminar un plan meal")
    void shouldDeletePlanMeal() throws Exception {

        mockMvc.perform(
                        delete("/api/plan-meals/" + meal.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk());

        assertFalse(planMealRepository.findById(meal.getId()).isPresent());
    }

    @Test
    @DisplayName("Eliminar un plan meal inexistente no produce error")
    void shouldIgnoreDeletingUnknownMeal() throws Exception {

        mockMvc.perform(
                        delete("/api/plan-meals/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN puede agregar una porción a una comida")
    void shouldAddFoodPortion() throws Exception {

        FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(
                food2.getId(),
                150.0,
                MeasureUnit.GRAM
        );

        mockMvc.perform(
                        post("/api/plan-meals/" + meal.getId() + "/portions")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foods.length()").value(2));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la comida no existe")
    void shouldReturnNotFoundWhenMealDoesNotExistAddingPortion() throws Exception {

        FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(
                food2.getId(),
                150.0,
                MeasureUnit.GRAM
        );

        mockMvc.perform(
                        post("/api/plan-meals/999999/portions")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el alimento no existe")
    void shouldReturnNotFoundWhenFoodDoesNotExistAddingPortion() throws Exception {

        FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(
                999999L,
                150.0,
                MeasureUnit.GRAM
        );

        mockMvc.perform(
                        post("/api/plan-meals/" + meal.getId() + "/portions")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 409 cuando el alimento ya existe en la comida")
    void shouldReturnConflictWhenFoodAlreadyExists() throws Exception {

        FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(
                chicken.getId(),
                100.0,
                MeasureUnit.GRAM
        );

        mockMvc.perform(
                        post("/api/plan-meals/" + meal.getId() + "/portions")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ADMIN puede eliminar una porción de una comida")
    void shouldRemoveFoodPortion() throws Exception {

        mockMvc.perform(
                        delete("/api/plan-meals/" + meal.getId()
                                + "/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foods.length()").value(0));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la comida no existe al eliminar una porción")
    void shouldReturnNotFoundWhenRemovingPortionFromUnknownMeal() throws Exception {

        mockMvc.perform(
                        delete("/api/plan-meals/999999/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la porción no existe")
    void shouldReturnNotFoundWhenPortionDoesNotExist() throws Exception {

        mockMvc.perform(
                        delete("/api/plan-meals/" + meal.getId() + "/portions/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede actualizar alimento y cantidad de una porción")
    void shouldUpdateFoodPortion() throws Exception {

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        food2.getId(),
                        250.0
                );

        mockMvc.perform(
                        patch("/api/plan-meals/" + meal.getId()
                                + "/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foods[0].foodId").value(food2.getId()))
                .andExpect(jsonPath("$.foods[0].quantity").value(250.0));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la comida no existe al actualizar una porción")
    void shouldReturnNotFoundWhenUpdatingUnknownMeal() throws Exception {

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        food2.getId(),
                        250.0
                );

        mockMvc.perform(
                        patch("/api/plan-meals/999999/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la porción no existe al actualizar")
    void shouldReturnNotFoundWhenUpdatingUnknownPortion() throws Exception {

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        food2.getId(),
                        250.0
                );

        mockMvc.perform(
                        patch("/api/plan-meals/" + meal.getId()
                                + "/portions/999999")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 404 cuando el alimento no existe al actualizar")
    void shouldReturnNotFoundWhenUpdatingWithUnknownFood() throws Exception {

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        999999L,
                        250.0
                );

        mockMvc.perform(
                        patch("/api/plan-meals/" + meal.getId()
                                + "/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 409 cuando el alimento ya existe en la comida al actualizar")
    void shouldReturnConflictWhenUpdatingWithDuplicateFood() throws Exception {

        PlanFoodPortion existing = PlanFoodPortion.of(
                meal,
                food2,
                100.0,
                MeasureUnit.GRAM
        );

        meal.addFoodPortion(existing);
        planFoodPortionRepository.save(existing);

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        food2.getId(),
                        250.0
                );

        mockMvc.perform(
                        patch("/api/plan-meals/" + meal.getId()
                                + "/portions/" + portion.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());
    }


}