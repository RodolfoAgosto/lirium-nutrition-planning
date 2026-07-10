package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientControllerIT extends AbstractIntegrationTest {

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;

    private User patient;
    private User otherPatient;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @BeforeEach
    void setup() {

        refreshTokenRepository.deleteAll();
        patientProfileRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(
                new User(
                        "admin@test.com",
                        passwordEncoder.encode("1234"),
                        "Admin",
                        "Test",
                        Role.ADMIN
                )
        );

        User nutritionist = userRepository.save(
                new User(
                        "nutri@test.com",
                        passwordEncoder.encode("1234"),
                        "Nutri",
                        "Test",
                        Role.NUTRITIONIST
                )
        );

        patient = userRepository.save(
                new User(
                        "patient@test.com",
                        passwordEncoder.encode("1234"),
                        "Juan",
                        "Perez",
                        Role.PATIENT
                )
        );

        otherPatient = userRepository.save(
                new User(
                        "other@test.com",
                        passwordEncoder.encode("1234"),
                        "Pedro",
                        "Gomez",
                        Role.PATIENT
                )
        );


        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);
    }


    @Test
    @DisplayName("ADMIN debe poder buscar pacientes")
    void shouldSearchPatientsAsAdmin() throws Exception {

        mockMvc.perform(get("/api/patients/search")
                        .header("Authorization", adminToken)
                        .param("firstName", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("patient@test.com"));
    }


    @Test
    @DisplayName("NUTRITIONIST debe poder buscar pacientes")
    void shouldSearchPatientsAsNutritionist() throws Exception {

        mockMvc.perform(get("/api/patients/search")
                        .header("Authorization", nutritionistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    @DisplayName("Debe obtener detalle de paciente propio")
    void shouldGetOwnPatientDetail() throws Exception {

        mockMvc.perform(get("/api/patients/{id}", patient.getId())
                        .header("Authorization", patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("patient@test.com"));
    }


    @Test
    @DisplayName("ADMIN debe poder obtener cualquier paciente")
    void shouldAllowAdminToGetPatientDetail() throws Exception {

        mockMvc.perform(get("/api/patients/{id}", patient.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("patient@test.com"));
    }


    @Test
    @DisplayName("Paciente no puede obtener otro paciente")
    void shouldRejectPatientAccessToOtherPatient() throws Exception {

        mockMvc.perform(get("/api/patients/{id}", otherPatient.getId())
                        .header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Usuario no autenticado no puede obtener paciente")
    void shouldRejectUnauthenticatedPatientAccess() throws Exception {

        mockMvc.perform(get("/api/patients/{id}", patient.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Paciente puede actualizar su propio perfil")
    void shouldUpdateOwnProfile() throws Exception {

        mockMvc.perform(put("/api/patients/{id}", patient.getId())
                        .header("Authorization", patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Juan Updated",
                            "lastName": "Perez Updated",
                            "dni": "12345678"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Juan Updated"));
    }

    @Test
    @DisplayName("Paciente no puede actualizar otro perfil")
    void shouldRejectUpdatingOtherPatientProfile() throws Exception {

        mockMvc.perform(put("/api/patients/{id}", otherPatient.getId())
                        .header("Authorization", patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Hack",
                            "lastName": "User"
                        }
                        """))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("ADMIN puede actualizar perfil de paciente")
    void shouldAllowAdminToUpdatePatient() throws Exception {

        mockMvc.perform(put("/api/patients/{id}", patient.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Admin Update",
                            "lastName": "Patient"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Admin Update"));
    }
}