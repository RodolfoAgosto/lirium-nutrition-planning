package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequestDTO;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.testdata.FoodTestDataFactory;
import com.lirium.nutrition.testdata.NutritionPlanTemplateTestDataFactory;
import com.lirium.nutrition.testdata.NutritionPlanTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


class NutritionPlanControllerIT extends AbstractIntegrationTest {

    private Long draftNutritionPlanId;

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private Long patientId;
    private Long otherPatientId;

    private PatientProfile patientProfile;
    private PatientProfile otherPatientProfile;

    private Long nutritionPlanId;
    private Long templateId;

    private UserDetails admin;

    private Long emptyPatientId;

    @Autowired
    private NutritionPlanTestDataFactory nutritionPlanTestDataFactory;

    @Autowired
    private NutritionPlanTemplateTestDataFactory nutritionPlanTemplateTestDataFactory;

    @Autowired
    private FoodTestDataFactory foodTestDataFactory;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @BeforeEach
    void setup() {

        nutritionPlanRepository.deleteAll();

        foodTestDataFactory.createAllRequiredFoods();

        User emptyPatient = new User(
                "empty@test.com",
                passwordEncoder.encode("1234"),
                "Empty",
                "Patient",
                Role.PATIENT
        );

        emptyPatient.setBirthDate(LocalDate.of(1995, 5, 10));

        PatientProfile emptyProfile = emptyPatient.getPatientProfile();

        emptyProfile.updateNutritionProfile(
                Height.of(175),
                Weight.of(70000),
                ActivityLevel.MODERATE,
                GoalType.WEIGHT_LOSS
        );

        User savedEmptyPatient = userRepository.save(emptyPatient);

        emptyPatientId = savedEmptyPatient.getPatientProfile().getId();

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin",
                "Test",
                Role.ADMIN));

        User nutritionist = userRepository.save(new User(
                "nutri@test.com",
                passwordEncoder.encode("1234"),
                "Nutri",
                "Test",
                Role.NUTRITIONIST));

        User patient = new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient",
                "Test",
                Role.PATIENT
        );

        patient.setBirthDate(LocalDate.of(1990, 1, 1));

        userRepository.save(patient);

        User otherPatient = userRepository.save(new User(
                "other@test.com",
                passwordEncoder.encode("1234"),
                "Other",
                "Test",
                Role.PATIENT));

        patientId = patient.getId();
        otherPatientId = otherPatient.getId();

        patientProfile = patient.getPatientProfile();
        otherPatientProfile = otherPatient.getPatientProfile();

        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);

        NutritionPlan plan = nutritionPlanTestDataFactory.createActivePlan(patientProfile);

        nutritionPlanId = plan.getId();

        NutritionPlanTemplate template = nutritionPlanTemplateTestDataFactory.createTemplate();

        templateId = template.getId();

        NutritionPlan draftPlan = nutritionPlanTestDataFactory.createDraftPlan(patientProfile);

        draftNutritionPlanId = draftPlan.getId();
    }

    @Test
    @DisplayName("ADMIN puede obtener NutritionPlan por id")
    void shouldReturnNutritionPlanWhenAdminRequestsById() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/" + nutritionPlanId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("NUTRITIONIST puede obtener NutritionPlan por id")
    void shouldReturnNutritionPlanWhenNutritionistRequestsById() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/" + nutritionPlanId)
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("PATIENT dueño puede obtener su NutritionPlan")
    void shouldReturnNutritionPlanWhenOwnerPatientRequestsById() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/" + nutritionPlanId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("PATIENT no puede obtener NutritionPlan de otro paciente")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsNutritionPlan()
            throws Exception {

        NutritionPlan otherPlan =
                nutritionPlanTestDataFactory.createActivePlan(otherPatientProfile);

        mockMvc.perform(
                        get("/api/nutrition-plans/" + otherPlan.getId())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario no autenticado recibe 401")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/" + nutritionPlanId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("NutritionPlan inexistente devuelve 404")
    void shouldReturnNotFoundWhenNutritionPlanDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede obtener los NutritionPlans de cualquier paciente")
    void shouldReturnPatientNutritionPlansWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + patientId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("NUTRITIONIST puede obtener los NutritionPlans de cualquier paciente")
    void shouldReturnPatientNutritionPlansWhenNutritionistRequests() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + patientId)
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("PATIENT dueño puede obtener sus NutritionPlans")
    void shouldReturnPatientNutritionPlansWhenOwnerRequests() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + patientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(nutritionPlanId));
    }

    @Test
    @DisplayName("PATIENT no puede obtener NutritionPlans de otro paciente")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsNutritionPlans()
            throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + otherPatientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario no autenticado recibe 401")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticatedForFindByPatient()
            throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + patientId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Paciente sin NutritionPlans devuelve lista vacía")
    void shouldReturnEmptyListWhenPatientHasNoNutritionPlans() throws Exception {

        mockMvc.perform(
                        get("/api/nutrition-plans/patient/" + otherPatientId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("ADMIN puede completar un NutritionPlan")
    void shouldCompleteNutritionPlanWhenAdminRequests() throws Exception {

        CompleteNutritionPlanRequestDTO dto =
                new CompleteNutritionPlanRequestDTO(
                        "Weight Loss Plan",
                        "Plan completed from integration test"
                );

        mockMvc.perform(
                        patch("/api/nutrition-plans/" + draftNutritionPlanId + "/complete")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(draftNutritionPlanId))
                .andExpect(jsonPath("$.name").value("Weight Loss Plan"));

    }

    @Test
    @DisplayName("NUTRITIONIST puede completar un NutritionPlan")
    void shouldCompleteNutritionPlanWhenNutritionistRequests() throws Exception {

        CompleteNutritionPlanRequestDTO dto =
                new CompleteNutritionPlanRequestDTO(
                        "Weight Loss Plan",
                        "Plan completed from integration test"
                );

        mockMvc.perform(
                        patch("/api/nutrition-plans/" + draftNutritionPlanId + "/complete")
                                .header("Authorization", nutritionistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(draftNutritionPlanId));
    }

    @Test
    @DisplayName("PATIENT no puede completar un NutritionPlan")
    void shouldReturnForbiddenWhenPatientCompletesNutritionPlan() throws Exception {

        CompleteNutritionPlanRequestDTO dto =
                new CompleteNutritionPlanRequestDTO(
                        "Weight Loss Plan",
                        "Plan completed from integration test"
                );

        mockMvc.perform(
                        patch("/api/nutrition-plans/" + nutritionPlanId + "/complete")
                                .header("Authorization", patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Complete NutritionPlan inexistente devuelve 404")
    void shouldReturnNotFoundWhenCompletingNonExistingNutritionPlan() throws Exception {

        CompleteNutritionPlanRequestDTO dto =
                new CompleteNutritionPlanRequestDTO(
                        "Weight Loss Plan",
                        "Plan completed from integration test"
                );

        mockMvc.perform(
                        patch("/api/nutrition-plans/999999/complete")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede generar un NutritionPlan para un paciente")
    void shouldGenerateNutritionPlanWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        post("/api/nutrition-plans/generate/" + emptyPatientId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.targetGoal").value("WEIGHT_LOSS"));


    }

    @Test
    @DisplayName("Generar NutritionPlan para paciente inexistente devuelve 404")
    void shouldReturnNotFoundWhenGeneratingForNonExistingPatient() throws Exception {

        mockMvc.perform(
                        post("/api/nutrition-plans/generate/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("No permite generar si el paciente ya tiene un plan DRAFT")
    void shouldFailWhenPatientAlreadyHasDraftPlan() throws Exception {

        nutritionPlanTestDataFactory.createDraftPlan(
                patientProfile
        );

        mockMvc.perform(
                        post("/api/nutrition-plans/generate/" + patientId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("ADMIN puede generar NutritionPlan desde template")
    void shouldGenerateNutritionPlanFromTemplate() throws Exception {

        mockMvc.perform(
                        post("/api/nutrition-plans/generate-from-template/"
                                + emptyPatientId + "/" + templateId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.targetGoal").exists());
    }


    @Test
    @DisplayName("Template inexistente devuelve 404")
    void shouldReturnNotFoundWhenTemplateDoesNotExist() throws Exception {

        mockMvc.perform(
                        post("/api/nutrition-plans/generate-from-template/"
                                + emptyPatientId + "/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede activar un NutritionPlan")
    void shouldActivateNutritionPlanWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        patch("/api/nutrition-plans/"
                                + draftNutritionPlanId + "/activate")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Activar NutritionPlan inexistente devuelve 404")
    void shouldReturnNotFoundWhenActivatingNonExistingPlan() throws Exception {

        mockMvc.perform(
                        patch("/api/nutrition-plans/999999/activate")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }
}