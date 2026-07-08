package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.testdata.DailyRecordTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyRecordControllerIT  extends AbstractIntegrationTest{

    // setup

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;
    private Long patientId;
    private Long otherPatientId;
    private PatientProfile patientProfile;
    private PatientProfile otherPatientProfile;
    private UserDetails admin;
    private Long dailyRecordId;
    private Long mealRecordId;
    private Long portionId;
    private Long foodId;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private DailyRecordTestDataFactory dailyRecordTestDataFactory;

    @BeforeEach
    void setup() {

        dailyRecordRepository.deleteAll();

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin", "Test", Role.ADMIN));

        User nutritionist = userRepository.save(new User(
                "nutri@test.com",
                passwordEncoder.encode("1234"),
                "Nutri", "Test", Role.NUTRITIONIST));

        User patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient", "Test", Role.PATIENT));

        User otherPatient = userRepository.save(new User(
                "other@test.com",
                passwordEncoder.encode("1234"),
                "Other", "Test", Role.PATIENT));

        patientId = patient.getId();
        otherPatientId = otherPatient.getId();

        patientProfile = patient.getPatientProfile();
        otherPatientProfile = otherPatient.getPatientProfile();

        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);

        DailyRecord record = dailyRecordTestDataFactory.createDailyRecord(
                patientProfile,
                LocalDate.now()
        );

        dailyRecordId = record.getId();

        MealRecord meal = record.getMeals().getFirst();
        mealRecordId = meal.getId();

        portionId = meal.getFoodPortions()
                .getFirst()
                .getId();

        portionId = meal.getFoodPortions()
                .getFirst()
                .getId();

        foodId = meal.getFoodPortions()
                .getFirst()
                .getFood()
                .getId();

    }

    @Test
    @DisplayName("should return today's daily record when admin requests patient record")
    void shouldReturnTodayDailyRecordWhenAdminRequestsPatientRecord() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    @DisplayName("should return today's daily record when nutritionist requests patient record")
    void shouldReturnTodayDailyRecordWhenNutritionistRequestsPatientRecord()
            throws Exception {
        mockMvc.perform(get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return today's daily record when patient requests own record")
    void shouldReturnTodayDailyRecordWhenPatientRequestsOwnRecord() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return forbidden when patient requests another patient's today record")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsTodayRecord()
            throws Exception {
        mockMvc.perform(
                        get("/api/daily-records/today/" + otherPatientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return unauthorized when user is not authenticated")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/today/" + patientId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return not found when patient does not exist")
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/daily-records/today/99999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    // GET /{id}
    @Test
    @DisplayName("ADMIN puede obtener DailyRecord por id")
    void shouldReturnDailyRecordWhenAdminRequestsById() throws Exception {

        mockMvc.perform(get("/api/daily-records/" + dailyRecordId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dailyRecordId));

    }

    @Test
    @DisplayName("NUTRITIONIST puede obtener DailyRecord por id")
    void shouldReturnDailyRecordWhenNutritionistRequestsById() throws Exception {


        mockMvc.perform(
                        get("/api/daily-records/" + dailyRecordId)
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dailyRecordId));

    }

    @Test
    @DisplayName("PATIENT dueño puede obtener su DailyRecord")
    void shouldReturnDailyRecordWhenOwnerPatientRequestsById() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/" + dailyRecordId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dailyRecordId));
    }

    @Test
    @DisplayName("PATIENT no puede obtener DailyRecord de otro paciente")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsRecordById()
            throws Exception {


        User other =
                userRepository.findById(otherPatientId)
                        .orElseThrow();


        DailyRecord otherRecord =
                DailyRecord.of(
                        other.getPatientProfile(),
                        LocalDate.now()
                );


        Long otherRecordId =
                dailyRecordRepository.save(otherRecord).getId();



        mockMvc.perform(
                        get("/api/daily-records/" + otherRecordId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("DailyRecord inexistente devuelve 404")
    void shouldReturnNotFoundWhenDailyRecordDoesNotExist()
            throws Exception {


        mockMvc.perform(
                        get("/api/daily-records/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());

    }

    // GET /patient/{patientId}

    @Test
    @DisplayName("ADMIN puede obtener los DailyRecords de cualquier paciente")
    void shouldReturnPatientDailyRecordsWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(dailyRecordId));
    }


    @Test
    @DisplayName("NUTRITIONIST puede obtener los DailyRecords de cualquier paciente")
    void shouldReturnPatientDailyRecordsWhenNutritionistRequests() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId)
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(dailyRecordId));
    }


    @Test
    @DisplayName("PATIENT dueño puede obtener sus propios DailyRecords")
    void shouldReturnPatientDailyRecordsWhenOwnerPatientRequests() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(dailyRecordId));
    }


    @Test
    @DisplayName("PATIENT no puede obtener DailyRecords de otro paciente")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsDailyRecords() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + otherPatientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Paciente sin DailyRecords devuelve lista vacía")
    void shouldReturnEmptyListWhenPatientHasNoDailyRecords() throws Exception {

        // paciente creado en setup pero sin registros
        Long patientWithoutRecordsId = otherPatientId;

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientWithoutRecordsId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("ADMIN puede obtener adherence de un paciente")
    void shouldReturnAdherenceWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId + "/adherence")
                                .param("from", LocalDate.now().minusDays(7).toString())
                                .param("to", LocalDate.now().toString())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT dueño puede obtener adherence")
    void shouldReturnAdherenceWhenOwnerPatientRequests() throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId + "/adherence")
                                .param("from", LocalDate.now().minusDays(7).toString())
                                .param("to", LocalDate.now().toString())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT no puede obtener adherence de otro paciente")
    void shouldReturnForbiddenWhenPatientRequestsOtherPatientAdherence()
            throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/patient/" + otherPatientId + "/adherence")
                                .param("from", LocalDate.now().minusDays(7).toString())
                                .param("to", LocalDate.now().toString())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN puede obtener nutrition comparison")
    void shouldReturnNutritionComparisonWhenAdminRequests() throws Exception {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                patientProfile
        );

        plan.activate(LocalDate.now());

        nutritionPlanRepository.save(plan);


        mockMvc.perform(
                        get("/api/daily-records/patient/" + patientId + "/nutrition-comparison")
                                .param("from", LocalDate.now().minusDays(7).toString())
                                .param("to", LocalDate.now().toString())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days").exists());
    }

    @Test
    void shouldAddPortionToMeal() throws Exception {

        FoodPortionAddRequestDTO dto =
                new FoodPortionAddRequestDTO(
                        foodId,
                        100d,
                        MeasureUnit.GRAM
                );

        mockMvc.perform(
                        post("/api/daily-records/meals/" + mealRecordId + "/portions")
                                .header("Authorization", patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());
    }



    @Test
    void shouldUpdateMeal() throws Exception {

        MealRecordUpdateRequestDTO dto =
                new MealRecordUpdateRequestDTO("Lunch");

        mockMvc.perform(
                        patch("/api/daily-records/meals/" + mealRecordId)
                                .header("Authorization", patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRemovePortion() throws Exception {

        mockMvc.perform(
                        delete("/api/daily-records/"
                                + dailyRecordId
                                + "/meals/"
                                + mealRecordId
                                + "/portions/"
                                + portionId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isNoContent());
    }


}