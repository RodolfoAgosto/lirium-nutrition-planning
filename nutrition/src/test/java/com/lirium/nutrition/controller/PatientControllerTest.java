package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.response.PatientDetailsDTO;
import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.service.PatientService;
import com.lirium.nutrition.service.UserService;
import com.lirium.nutrition.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchPatients() throws Exception {

        List<PatientSummaryDTO> response = List.of(
                new PatientSummaryDTO(
                        1L,
                        "Juan",
                        "Perez",
                        "juan@test.com",
                        "12345678"
                )
        );

        when(patientService.searchPatients(
                "Juan",
                null,
                null,
                null
        )).thenReturn(response);

        mockMvc.perform(get("/api/patients/search")
                        .param("firstName", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Juan"))
                .andExpect(jsonPath("$[0].lastName").value("Perez"))
                .andExpect(jsonPath("$[0].email").value("juan@test.com"))
                .andExpect(jsonPath("$[0].dni").value("12345678"));

        verify(patientService)
                .searchPatients(
                        "Juan",
                        null,
                        null,
                        null
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetPatient() throws Exception {

        Height height = mock(Height.class);
        Weight weight = mock(Weight.class);

        PatientDetailsDTO response =
                new PatientDetailsDTO(
                        1L,
                        "Juan",
                        "Perez",
                        "juan@test.com",
                        "12345678",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990, 1, 1),
                        height,
                        weight,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_LOSS,
                        "No notes",
                        Set.of(),
                        Set.of()
                );

        when(patientService.getPatientDetail(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Perez"))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.dni").value("12345678"));

        verify(patientService)
                .getPatientDetail(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdatePatient() throws Exception {

        PatientUpdateRequestDTO request =
                new PatientUpdateRequestDTO(
                        "Juan Updated",
                        "Perez",
                        "juan@test.com",
                        "12345678",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990, 1, 1),
                        180,
                        80,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_LOSS,
                        "Updated notes",
                        Set.of(),
                        List.of()
                );

        Height height = mock(Height.class);
        Weight weight = mock(Weight.class);

        PatientDetailsDTO response =
                new PatientDetailsDTO(
                        1L,
                        "Juan Updated",
                        "Perez",
                        "juan@test.com",
                        "12345678",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990, 1, 1),
                        height,
                        weight,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_LOSS,
                        "Updated notes",
                        Set.of(),
                        Set.of()
                );

        when(patientService.updatePatient(
                eq(1L),
                any(PatientUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan Updated"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        verify(patientService)
                .updatePatient(
                        eq(1L),
                        any(PatientUpdateRequestDTO.class));
    }
}