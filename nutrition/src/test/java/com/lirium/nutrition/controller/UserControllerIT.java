package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractIntegrationTest {

    // setup

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private Long adminId;
    private Long nutritionistId;
    private Long patientId;
    private Long disabledUserId;

    private User admin;
    private User nutritionist;
    private User patient;
    private User disabled;

    @BeforeEach
    void setup() {

        refreshTokenRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("12345678"),
                "Admin",
                "Test",
                Role.ADMIN));

        nutritionist = userRepository.save(new User(
                "nutritionist@test.com",
                passwordEncoder.encode("12345678"),
                "Nutritionist",
                "Test",
                Role.NUTRITIONIST));

        patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("12345678"),
                "Patient",
                "Test",
                Role.PATIENT));

        disabled = userRepository.save(new User(
                "disabled@test.com",
                passwordEncoder.encode("12345678"),
                "Disabled",
                "User",
                Role.PATIENT));

        disabled.setEnabled(false);

        adminId = admin.getId();
        nutritionistId = nutritionist.getId();
        patientId = patient.getId();
        disabledUserId = disabled.getId();

        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);
    }

    @Test
    @DisplayName("Debe registrar un usuario")
    void shouldRegisterUser() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "newuser@test.com",
                "12345678",
                "Juan",
                "Perez",
                LocalDate.of(1990, 1, 1),
                "40111222"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.enabled").value(true));

        User saved = userRepository.findByEmail("newuser@test.com").orElseThrow();

        assertTrue(passwordEncoder.matches("12345678", saved.getPasswordHash()));
    }

    @Test
    @DisplayName("No debe registrar un usuario con email repetido")
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                admin.getEmail(),
                "12345678",
                "Juan",
                "Perez",
                LocalDate.of(1990, 1, 1),
                "40111222"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Debe retornar BadRequest cuando el request es inválido")
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "",
                "123",
                "",
                "",
                LocalDate.now().plusDays(1),
                "abc"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN puede registrar un paciente")
    void shouldRegisterPatientWhenAdminRequests() throws Exception {

        CreatePatientRequestDTO request = new CreatePatientRequestDTO(
                "patient2@test.com",
                "Pedro",
                "Gomez",
                LocalDate.of(1995, 5, 10),
                "40555111"
        );

        mockMvc.perform(post("/api/users/patient")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("patient2@test.com"));
    }

    @Test
    @DisplayName("PATIENT no puede registrar pacientes")
    void shouldReturnForbiddenWhenPatientRegistersPatient() throws Exception {

        CreatePatientRequestDTO request = new CreatePatientRequestDTO(
                "patient2@test.com",
                "Pedro",
                "Gomez",
                LocalDate.of(1995, 5, 10),
                "40555111"
        );

        mockMvc.perform(post("/api/users/patient")
                        .header("Authorization", patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("No debe registrar un paciente con email repetido")
    void shouldReturnConflictWhenPatientEmailAlreadyExists() throws Exception {

        CreatePatientRequestDTO request = new CreatePatientRequestDTO(
                patient.getEmail(),
                "Pedro",
                "Gomez",
                LocalDate.of(1995, 5, 10),
                "40555111"
        );

        mockMvc.perform(post("/api/users/patient")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ADMIN puede obtener un usuario por id")
    void shouldFindUserByIdWhenAdminRequests() throws Exception {

        mockMvc.perform(get("/api/users/" + patient.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.email").value(patient.getEmail()));
    }

    @Test
    @DisplayName("Debe retornar NotFound cuando el usuario no existe")
    void shouldReturnNotFoundWhenFindingUnknownUser() throws Exception {

        mockMvc.perform(get("/api/users/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede obtener un usuario por email")
    void shouldFindUserByEmailWhenAdminRequests() throws Exception {

        mockMvc.perform(get("/api/users/email")
                        .param("email", patient.getEmail())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.email").value(patient.getEmail()));
    }

    @Test
    @DisplayName("Debe retornar NotFound cuando el email no existe")
    void shouldReturnNotFoundWhenFindingUnknownEmail() throws Exception {

        mockMvc.perform(get("/api/users/email")
                        .param("email", "unknown@test.com")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede listar todos los usuarios")
    void shouldFindAllUsersWhenAdminRequests() throws Exception {

        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));

    }

    @Test
    @DisplayName("ADMIN puede actualizar los datos básicos de un usuario")
    void shouldUpdateBasicInfoWhenAdminRequests() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO(
                "Juan Carlos",
                "Gonzalez",
                LocalDate.of(1985, 6, 15)
        );

        mockMvc.perform(
                        put("/api/users/" + patient.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.lastName").value("Gonzalez"));

        User updated = userRepository.findById(patient.getId()).orElseThrow();

        assertEquals("Juan Carlos", updated.getFirstName());
        assertEquals("Gonzalez", updated.getLastName());
        assertEquals(LocalDate.of(1985, 6, 15), updated.getBirthDate());
    }

    @Test
    @DisplayName("Debe retornar NotFound al actualizar un usuario inexistente")
    void shouldReturnNotFoundWhenUpdatingUnknownUser() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO(
                "Juan",
                "Perez",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(
                        put("/api/users/999999")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NUTRITIONIST puede actualizar un usuario")
    void shouldUpdateBasicInfoWhenNutritionistRequests() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO(
                "Nuevo",
                "Apellido",
                LocalDate.of(1995, 5, 5)
        );

        mockMvc.perform(
                        put("/api/users/" + patient.getId())
                                .header("Authorization", nutritionistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Nuevo"));
    }

    @Test
    @DisplayName("PATIENT no puede actualizar un usuario")
    void shouldReturnForbiddenWhenPatientUpdatesUser() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO(
                "Juan",
                "Perez",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(
                        put("/api/users/" + patient.getId())
                                .header("Authorization", patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN puede deshabilitar un usuario")
    void shouldDisableUserWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        patch("/api/users/" + patient.getId() + "/enabled")
                                .param("enabled", "false")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        User updated = userRepository.findById(patient.getId()).orElseThrow();

        assertFalse(updated.isEnabled());
    }

    @Test
    @DisplayName("Debe retornar NotFound al cambiar el estado de un usuario inexistente")
    void shouldReturnNotFoundWhenChangingEnabledUnknownUser() throws Exception {

        mockMvc.perform(
                        patch("/api/users/999999/enabled")
                                .param("enabled", "false")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NUTRITIONIST no puede habilitar o deshabilitar usuarios")
    void shouldReturnForbiddenWhenNutritionistChangesEnabled() throws Exception {

        mockMvc.perform(
                        patch("/api/users/" + patient.getId() + "/enabled")
                                .param("enabled", "false")
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN puede validar el email de un usuario")
    void shouldValidateEmailWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        patch("/api/users/" + patient.getId() + "/validate-email")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailValidated").value(true));

        User updated = userRepository.findById(patient.getId()).orElseThrow();

        assertTrue(updated.getEmailValidated());
    }

    @Test
    @DisplayName("Debe retornar NotFound al validar el email de un usuario inexistente")
    void shouldReturnNotFoundWhenValidatingUnknownUser() throws Exception {

        mockMvc.perform(
                        patch("/api/users/999999/validate-email")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NUTRITIONIST no puede validar el email de un usuario")
    void shouldReturnForbiddenWhenNutritionistValidatesEmail() throws Exception {

        mockMvc.perform(
                        patch("/api/users/" + patient.getId() + "/validate-email")
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN puede eliminar un usuario")
    void shouldDeleteUserWhenAdminRequests() throws Exception {

        mockMvc.perform(
                        delete("/api/users/" + patient.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(patient.getId()).orElseThrow();

        assertFalse(updated.isEnabled());
    }

    @Test
    @DisplayName("Debe retornar NotFound al eliminar un usuario inexistente")
    void shouldReturnNotFoundWhenDeletingUnknownUser() throws Exception {

        mockMvc.perform(
                        delete("/api/users/999999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe retornar BadRequest cuando el usuario ya está deshabilitado")
    void shouldReturnBadRequestWhenDeletingDisabledUser() throws Exception {

        mockMvc.perform(
                        delete("/api/users/" + disabled.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("NUTRITIONIST no puede eliminar usuarios")
    void shouldReturnForbiddenWhenNutritionistDeletesUser() throws Exception {

        mockMvc.perform(
                        delete("/api/users/" + patient.getId())
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATIENT no puede eliminar usuarios")
    void shouldReturnForbiddenWhenPatientDeletesUser() throws Exception {

        mockMvc.perform(
                        delete("/api/users/" + patient.getId())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }


}