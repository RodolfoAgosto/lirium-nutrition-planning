package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.infrastructure.security.PatientSecurity;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.UserRepository;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import com.lirium.nutrition.infrastructure.security.JwtAuthenticationFilter; // Importalo
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        mvc.perform(get("/api/daily-records/today/{patientId}", targetPatientId)
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

        DailyRecordResponseDTO response = mock(DailyRecordResponseDTO.class);

        when(dailyRecordService.getOrCreateToday(99L))
                .thenReturn(response);

        mvc.perform(get("/api/daily-records/today/{patientId}", 99L))
                .andExpect(status().isOk());

        verify(dailyRecordService).getOrCreateToday(99L);
    }

    @Test
    @WithMockUser(roles = "NUTRITIONIST")
    void shouldAllowNutritionistToAccessAnyPatientRecord() throws Exception {

        DailyRecordResponseDTO response = mock(DailyRecordResponseDTO.class);

        when(dailyRecordService.getOrCreateToday(99L))
                .thenReturn(response);

        mvc.perform(get("/api/daily-records/today/{patientId}", 99L))
                .andExpect(status().isOk());

        verify(dailyRecordService).getOrCreateToday(99L);
    }

}