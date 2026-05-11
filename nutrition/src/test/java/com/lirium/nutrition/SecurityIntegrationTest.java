package com.lirium.nutrition;

import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PatientProfileRepository patientRepository;
    @Autowired
    FoodRepository foodRepository;

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;
    private Long patientId;
    private Long otherPatientId;
    private PatientProfile patientProfile;
    private PatientProfile otherPatientProfile;

    @BeforeEach
    void setup() {

        patientRepository.deleteAll();
        userRepository.deleteAll();
        foodRepository.deleteAll();


        User admin = userRepository.save(new User(
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
    }

    // Tests de Autenticación
    @Test
    @DisplayName("Login exitoso devuelve token")
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login con password incorrecta devuelve 401")
    void loginWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "admin@test.com",
                    "password": "wrongpassword"
                }
            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con usuario inexistente devuelve 401")
    void loginUnknownUser() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "noexiste@test.com",
                    "password": "1234"
                }
            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request sin token devuelve 401")
    void requestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request con token inválido devuelve 401")
    void requestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer tokeninvalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN puede listar usuarios")
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("NUTRITIONIST puede listar usuarios")
    void nutritionistCanListUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT no puede listar usuarios")
    void patientCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    // Tests de Roles — Usuarios
    @Test
    @DisplayName("Solo ADMIN puede eliminar usuario")
    void onlyAdminCanDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + patientId)
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/" + patientId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Solo ADMIN puede habilitar/deshabilitar usuario")
    void onlyAdminCanToggleEnabled() throws Exception {
        mockMvc.perform(
                patch("/api/users/" + patientId + "/enabled")
                        .param("enabled", "true")
                        .header("Authorization", nutritionistToken)
        ).andExpect(status().isForbidden());
        mockMvc.perform(
                patch("/api/users/" + patientId + "/enabled")
                        .param("enabled", "true")
                        .header("Authorization", adminToken)
        ).andExpect(status().isOk());
    }

    // Tests de Ownership — Pacientes
    @Test
    @DisplayName("PATIENT puede ver su propio perfil")
    void patientCanViewOwnProfile() throws Exception {
        mockMvc.perform(get("/patients/" + patientProfile.getId())
                        .header("Authorization", patientToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT no puede ver perfil de otro paciente")
    void patientCannotViewOtherProfile() throws Exception {
        mockMvc.perform(get("/patients/" + otherPatientProfile.getId())
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("NUTRITIONIST puede ver cualquier paciente")
    void nutritionistCanViewAnyPatient() throws Exception {
        mockMvc.perform(get("/patients/" + patientProfile.getId())
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/patients/" + otherPatientProfile.getId())
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT puede ver sus planes")
    void patientCanViewOwnPlans() throws Exception {
        mockMvc.perform(get("/api/nutrition-plans/patient/" + patientId)
                        .header("Authorization", patientToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT no puede ver planes de otro paciente")
    void patientCannotViewOtherPatientPlans() throws Exception {
        mockMvc.perform(get("/api/nutrition-plans/patient/" + otherPatientId)
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    // Tests de Daily Records con Ownership
    @Test
    @DisplayName("PATIENT puede ver sus registros diarios")
    void patientCanViewOwnDailyRecords() throws Exception {
        mockMvc.perform(get("/api/daily-records/patient/" + patientId)
                        .header("Authorization", patientToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATIENT no puede ver registros de otro paciente")
    void patientCannotViewOtherDailyRecords() throws Exception {
        mockMvc.perform(get("/api/daily-records/patient/" + otherPatientId)
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("NUTRITIONIST puede ver adherencia de cualquier paciente")
    void nutritionistCanViewAnyAdherence() throws Exception {
        mockMvc.perform(get("/api/daily-records/patient/"
                        + patientId + "/adherence")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-09")
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());
    }

    // Tests de Foods
    @Test
    @DisplayName("Todos los roles pueden ver alimentos")
    void allRolesCanViewFoods() throws Exception {
        mockMvc.perform(get("/api/foods")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", patientToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Solo ADMIN puede crear alimentos")
    void onlyAdminCanCreateFood() throws Exception {
        String body = """
             {
               "name": "Arroz Yamanitototototo",
               "caloriesPer100g": 350,
               "proteinPer100g": 7,
               "carbsPer100g": 77,
               "fatPer100g": 1,
               "category": "VEGETABLE"
             }
    """;

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", nutritionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

}
