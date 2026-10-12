package com.lirium.nutrition.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.testdata.NutritionPlanTestDataFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * Walks the same path the patient web client follows: profile, active plan, today's record, a
 * modified meal and the two reports that reflect it.
 */
class PatientJourneyIT extends AbstractIntegrationTest {

  private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

  @Autowired private NutritionPlanRepository nutritionPlanRepository;

  @Autowired private NutritionPlanTestDataFactory nutritionPlanTestDataFactory;

  private Long patientId;
  private Long otherPatientId;
  private String patientToken;
  private String otherPatientToken;

  @BeforeEach
  void setup() {

    dailyRecordRepository.deleteAll();
    nutritionPlanRepository.deleteAll();

    User patient =
        userRepository.save(
            new User(
                "journey@test.com",
                passwordEncoder.encode("1234"),
                "Journey",
                "Patient",
                Role.PATIENT));

    User otherPatient =
        userRepository.save(
            new User(
                "journey-other@test.com",
                passwordEncoder.encode("1234"),
                "Journey",
                "Other",
                Role.PATIENT));

    patientId = patient.getId();
    otherPatientId = otherPatient.getId();

    NutritionPlan plan = nutritionPlanTestDataFactory.createDraftPlan(patient.getPatientProfile());
    plan.activate(LocalDate.now(ARGENTINA_ZONE));
    nutritionPlanRepository.save(plan);

    patientToken = "Bearer " + jwtService.generateToken(patient);
    otherPatientToken = "Bearer " + jwtService.generateToken(otherPatient);
  }

  @Test
  @DisplayName("patient logs a modified meal and both reports reflect it")
  void shouldReflectModifiedMealInReportsWhenPatientCompletesJourney() throws Exception {

    // 1. The client learns who the patient is from the token alone
    mockMvc
        .perform(get("/api/patients/me").header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.patientId").value(patientId))
        .andExpect(jsonPath("$.email").value("journey@test.com"));

    // 2. The active plan is visible
    mockMvc
        .perform(
            get("/api/patients/" + patientId + "/nutrition-plans/active")
                .header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.week.length()").value(7));

    // 3. Today's record is created from the plan the first time...
    String created =
        mockMvc
            .perform(
                post("/api/patients/" + patientId + "/daily-records/ensure")
                    .header("Authorization", patientToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.meals.length()").value(5))
            .andExpect(jsonPath("$.meals[0].overridden").value(false))
            .andReturn()
            .getResponse()
            .getContentAsString();

    // ...and returned as is the second time
    mockMvc
        .perform(
            post("/api/patients/" + patientId + "/daily-records/ensure")
                .header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meals.length()").value(5));

    JsonNode firstMeal = objectMapper.readTree(created).get("meals").get(0);
    Long mealId = firstMeal.get("id").asLong();

    // 4. Before any change, every meal counts as followed
    mockMvc
        .perform(
            get("/api/patients/" + patientId + "/daily-records/meal-adherence")
                .header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.days.length()").value(1))
        .andExpect(jsonPath("$.days[0].hasRecord").value(true))
        .andExpect(jsonPath("$.days[0].expectedMeals").value(5))
        .andExpect(jsonPath("$.days[0].followedMeals").value(5))
        .andExpect(jsonPath("$.days[0].modifiedMeals").value(0));

    // 5. The patient changes the amount of a planned food: the meal becomes modified
    Food chicken = foodRepository.findByName("Chicken Breast").orElseThrow();

    mockMvc
        .perform(
            post("/api/daily-records/meals/" + mealId + "/portions")
                .header("Authorization", patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new FoodPortionAddRequestDTO(chicken.getId(), 250.0, MeasureUnit.GRAM))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(mealId))
        .andExpect(jsonPath("$.overridden").value(true))
        .andExpect(jsonPath("$.portions.length()").value(1))
        .andExpect(jsonPath("$.portions[0].quantity").value(250.0));

    // 6. Adherence now shows one modified meal
    mockMvc
        .perform(
            get("/api/patients/" + patientId + "/daily-records/meal-adherence")
                .header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.days[0].followedMeals").value(4))
        .andExpect(jsonPath("$.days[0].modifiedMeals").value(1))
        .andExpect(jsonPath("$.summary.recordedDays").value(1));

    // 7. The nutrition comparison has consumed values and a score for the day
    mockMvc
        .perform(
            get("/api/patients/" + patientId + "/daily-records/nutrition-comparison")
                .header("Authorization", patientToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.days.length()").value(1))
        .andExpect(jsonPath("$.days[0].hasRecord").value(true))
        .andExpect(jsonPath("$.days[0].consumed").isNotEmpty())
        .andExpect(jsonPath("$.days[0].score").isNotEmpty())
        .andExpect(jsonPath("$.summary.recordedDays").value(1));
  }

  @Test
  @DisplayName("patient cannot reach another patient's journey endpoints")
  void shouldReturnForbiddenWhenPatientTouchesAnotherPatientsData() throws Exception {

    mockMvc
        .perform(
            get("/api/patients/" + otherPatientId + "/nutrition-plans/active")
                .header("Authorization", patientToken))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/api/patients/" + otherPatientId + "/daily-records/ensure")
                .header("Authorization", patientToken))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/patients/" + otherPatientId + "/daily-records/meal-adherence")
                .header("Authorization", patientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("patient cannot modify a meal from another patient's record")
  void shouldReturnForbiddenWhenPatientModifiesAnotherPatientsMeal() throws Exception {

    String created =
        mockMvc
            .perform(
                post("/api/patients/" + patientId + "/daily-records/ensure")
                    .header("Authorization", patientToken))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long mealId = objectMapper.readTree(created).get("meals").get(0).get("id").asLong();
    Food chicken = foodRepository.findByName("Chicken Breast").orElseThrow();

    mockMvc
        .perform(
            post("/api/daily-records/meals/" + mealId + "/portions")
                .header("Authorization", otherPatientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new FoodPortionAddRequestDTO(chicken.getId(), 250.0, MeasureUnit.GRAM))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("patient without an active plan cannot create today's record")
  void shouldReturnBadRequestWhenPatientHasNoActivePlan() throws Exception {

    mockMvc
        .perform(
            post("/api/patients/" + otherPatientId + "/daily-records/ensure")
                .header("Authorization", otherPatientToken))
        .andExpect(status().isBadRequest());
  }
}
