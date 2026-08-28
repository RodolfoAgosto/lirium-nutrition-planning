package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.infrastructure.security.JwtAuthenticationFilter; // Importalo
import com.lirium.nutrition.infrastructure.security.PatientSecurity;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.UserRepository;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DailyRecordController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        PatientSecurity.class,
        DailyRecordControllerSecurityTest.TestSecurityConfig.class
})
public class DailyRecordControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private DailyRecordService dailyRecordService;

    @MockBean
    private AdherenceReportService adherenceReportService;

    @MockBean
    private PatientSecurity patientSecurity;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PatientProfileRepository patientProfileRepository;

    @MockBean
    private Clock clock;

    @TestConfiguration
    @EnableMethodSecurity // Requerido para procesar las expresiones SpEL de @PreAuthorize
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Permitimos el paso HTTP plano
                    .build();
        }
    }

    @Test
    void shouldReturnForbiddenWhenUserAccessesAnotherPatient() throws Exception {
        Long loggedInUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);
        when(principal.getId()).thenReturn(loggedInUserId);
        when(principal.getUsername()).thenReturn("user@lirium.com");
        when(principal.getAuthorities()).thenReturn(Collections.emptyList()); // Sin roles ADMIN/NUTRITIONIST

        mvc.perform(get("/api/daily-records/patient/{patientId}", targetPatientId)
                        .with(user(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenUserAccessesAnotherPatientRecords() throws Exception {

        Long loggedInUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedInUserId);
        when(principal.getUsername()).thenReturn("patient@test.com");
        when(principal.getAuthorities()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/daily-records/patient/{patientId}", targetPatientId)
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dailyRecordService);
    }

    @Test
    void shouldReturnForbiddenWhenUserAccessesAnotherPatientAdherenceReport() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(principal.getUsername()).thenReturn("patient@test.com");
        when(principal.getAuthorities()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/daily-records/patient/{patientId}/adherence", targetPatientId)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-07")
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adherenceReportService);
    }

    @Test
    void shouldReturnForbiddenWhenUserAccessesAnotherPatientNutritionComparison() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(principal.getUsername()).thenReturn("test@test.com");
        when(principal.getAuthorities()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/daily-records/patient/{patientId}/nutrition-comparison", targetPatientId)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-07")
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dailyRecordService);
    }

    @Test
    void shouldReturnForbiddenWhenPatientAccessesOtherPatientRecord() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(patientSecurity.isOwner(eq(targetPatientId), any(Authentication.class)))
                .thenReturn(false);
        when(principal.getUsername()).thenReturn("patient@test.com");

        // Endpoint actualizado: GET /api/daily-records/patient/{patientId}
        mvc.perform(get("/api/daily-records/patient/{patientId}", targetPatientId)
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dailyRecordService);
    }

    @Test
    void shouldReturnForbiddenWhenPatientAccessesOtherPatientRecords() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(principal.getUsername()).thenReturn("patient@test.com");

        mvc.perform(get("/api/daily-records/patient/{patientId}", targetPatientId)
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dailyRecordService);
    }

    @Test
    void shouldReturnForbiddenWhenPatientRequestsOtherPatientAdherence() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(principal.getUsername()).thenReturn("test@test.com");

        mvc.perform(get("/api/daily-records/patient/{patientId}/adherence", targetPatientId)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-07")
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adherenceReportService);
    }

    @Test
    void shouldReturnForbiddenWhenPatientRequestsOtherPatientNutritionComparison() throws Exception {

        Long loggedUserId = 1L;
        Long targetPatientId = 99L;

        User principal = mock(User.class);

        when(principal.getId()).thenReturn(loggedUserId);
        when(principal.getUsername()).thenReturn("test@test.com");

        mvc.perform(get("/api/daily-records/patient/{patientId}/nutrition-comparison", targetPatientId)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-07")
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dailyRecordService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToAccessAnyPatientRecord() throws Exception {
        // Arrange
        Long patientId = 99L;
        LocalDate targetDate = LocalDate.now();

        DailyRecordResponseDTO mockResponse = new DailyRecordResponseDTO(
                1L,
                targetDate,
                Collections.emptyList()
        );

        when(dailyRecordService.getOrCreateForDate(eq(patientId), any(LocalDate.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mvc.perform(post("/api/daily-records/patient/{patientId}/ensure", patientId)
                        .param("date", targetDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(dailyRecordService).getOrCreateForDate(eq(patientId), eq(targetDate));
    }


    @Test
    @WithMockUser(roles = "NUTRITIONIST")
    void shouldAllowNutritionistToAccessAnyPatientRecord() throws Exception {
        // Arrange
        Long patientId = 99L;
        LocalDate targetDate = LocalDate.now();

        DailyRecordResponseDTO mockResponse = new DailyRecordResponseDTO(
                1L,
                targetDate,
                Collections.emptyList()
        );

        when(dailyRecordService.getOrCreateForDate(eq(patientId), any(LocalDate.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mvc.perform(post("/api/daily-records/patient/{patientId}/ensure", patientId)
                        .param("date", targetDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(dailyRecordService).getOrCreateForDate(eq(patientId), eq(targetDate));
    }

}