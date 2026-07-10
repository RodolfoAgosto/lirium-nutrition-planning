package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.testdata.NutritionPlanTemplateTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NutritionPlanTemplateControllerIT extends AbstractIntegrationTest {

    @Autowired
    private NutritionPlanTemplateRepository nutritionPlanTemplateRepository;

    @Autowired
    private NutritionPlanTemplateTestDataFactory nutritionPlanTemplateTestDataFactory;

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private User admin;
    private User nutritionist;
    private User patient;

    private NutritionPlanTemplate template;

    @BeforeEach
    void setup() {

        nutritionPlanTemplateRepository.deleteAll();

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

        template = nutritionPlanTemplateTestDataFactory.createTemplate();
    }

    @Test
    @DisplayName("Debe listar las plantillas")
    void shouldGetAllTemplates() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Debe obtener una plantilla por id")
    void shouldGetTemplateById() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates/{id}", template.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(template.getId()))
                .andExpect(jsonPath("$.name").value(template.getName()));
    }

    @Test
    @DisplayName("Debe retornar 404 cuando la plantilla no existe")
    void shouldReturnNotFoundWhenTemplateDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates/{id}", 999999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe crear una plantilla")
    void shouldCreateTemplate() throws Exception {

        String body = """
                {
                  "name":"Muscle Gain",
                  "description":"Template for muscle gain",
                  "targetGoal":"MUSCLE_GAIN",
                  "proteinPercentage":30,
                  "carbPercentage":40,
                  "fatPercentage":30,
                  "excludedTags":[]
                }
                """;

        mockMvc.perform(post("/api/nutrition-plan-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Muscle Gain"));

        assertTrue(nutritionPlanTemplateRepository.existsByName("Muscle Gain"));
    }

    @Test
    @DisplayName("Debe retornar 409 cuando el nombre ya existe")
    void shouldReturnConflictWhenTemplateAlreadyExists() throws Exception {

        String body = """
                {
                  "name":"Weight Loss Template",
                  "description":"Duplicated",
                  "targetGoal":"WEIGHT_LOSS",
                  "proteinPercentage":30,
                  "carbPercentage":40,
                  "fatPercentage":30,
                  "excludedTags":[]
                }
                """;

        mockMvc.perform(post("/api/nutrition-plan-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Debe actualizar una plantilla")
    void shouldUpdateTemplate() throws Exception {

        String body = """
                {
                   "name":"Updated Template",
                   "description":"Updated description",
                   "targetGoal":"MUSCLE_GAIN",
                   "proteinPercentage":25,
                   "carbPercentage":50,
                   "fatPercentage":25,
                   "excludedTags":[]
                }
                """;

        mockMvc.perform(put("/api/nutrition-plan-templates/{id}", template.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Template"));

        NutritionPlanTemplate updated =
                nutritionPlanTemplateRepository.findById(template.getId()).orElseThrow();

        assertEquals("Updated Template", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    @DisplayName("Debe retornar 404 al actualizar una plantilla inexistente")
    void shouldReturnNotFoundWhenUpdatingUnknownTemplate() throws Exception {

        String body = """
                {
                  "name":"Updated Template"
                }
                """;

        mockMvc.perform(put("/api/nutrition-plan-templates/{id}", 999999L)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe eliminar una plantilla")
    void shouldDeleteTemplate() throws Exception {

        mockMvc.perform(delete("/api/nutrition-plan-templates/{id}", template.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        assertFalse(nutritionPlanTemplateRepository.findById(template.getId()).isPresent());
    }

    @Test
    @DisplayName("Debe retornar 404 al eliminar una plantilla inexistente")
    void shouldReturnNotFoundWhenDeletingUnknownTemplate() throws Exception {

        mockMvc.perform(delete("/api/nutrition-plan-templates/{id}", 999999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el request de creación es inválido")
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {

        String body = """
                {
                  "name":"",
                  "description":"",
                  "proteinPercentage":30,
                  "carbPercentage":30,
                  "fatPercentage":30
                }
                """;

        mockMvc.perform(post("/api/nutrition-plan-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe permitir acceso al NUTRITIONIST")
    void shouldAllowNutritionist() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates")
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe denegar acceso al PATIENT")
    void shouldDenyPatient() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates")
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe retornar 401 cuando el usuario no está autenticado")
    void shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/api/nutrition-plan-templates"))
                .andExpect(status().isUnauthorized());
    }


}
