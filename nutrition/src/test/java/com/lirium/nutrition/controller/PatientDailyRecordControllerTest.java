package com.lirium.nutrition.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.TokenBlacklistService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientDailyRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientDailyRecordControllerTest {

  @MockBean private Clock clock;

  @MockBean private DailyRecordService dailyRecordService;

  @MockBean private AdherenceReportService adherenceReportService;

  @MockBean JwtService jwtService;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean UserDetailsServiceImpl userDetailsService;

  @Autowired MockMvc mvc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldCreateDailyRecordWhenItDoesNotExist() throws Exception {

    // Given
    Long patientId = 99L;
    LocalDate targetDate = LocalDate.now();

    DailyRecordResponseDTO mockResponse =
        new DailyRecordResponseDTO(1L, targetDate, Collections.emptyList());

    when(dailyRecordService.existsForPatientAndDate(patientId, targetDate)).thenReturn(false);

    when(dailyRecordService.getOrCreateForDate(patientId, targetDate)).thenReturn(mockResponse);

    // When + Then
    mvc.perform(
            post("/api/patients/{patientId}/daily-records/ensure", patientId)
                .param("date", targetDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.date").value(targetDate.toString()));

    verify(dailyRecordService).existsForPatientAndDate(patientId, targetDate);

    verify(dailyRecordService).getOrCreateForDate(patientId, targetDate);
  }

  // getByPatient
  @Test
  @WithMockUser(roles = "PATIENT")
  void shouldReturnPatientRecordsWhenPatientAccessesOwnRecords() throws Exception {

    List<DailyRecordResponseDTO> records =
        List.of(
            new DailyRecordResponseDTO(1L, LocalDate.of(2025, 1, 15), List.of()),
            new DailyRecordResponseDTO(2L, LocalDate.of(2025, 1, 16), List.of()));

    when(dailyRecordService.getByPatient(1L)).thenReturn(records);

    mvc.perform(get("/api/patients/1/daily-records").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2));

    verify(dailyRecordService).getByPatient(1L);
  }

  // adherence
  @Test
  @WithMockUser
  void shouldReturnAdherenceReportWhenPatientAccessesOwnData() throws Exception {

    Long patientId = 1L;
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 7);

    AdherenceReportDTO response = new AdherenceReportDTO(from, to, 21, 18, 85.7, List.of());

    when(adherenceReportService.getAdherence(patientId, from, to)).thenReturn(response);

    mvc.perform(
            get("/api/patients/{patientId}/daily-records/adherence", patientId)
                .param("from", from.toString())
                .param("to", to.toString())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.from").value(from.toString()))
        .andExpect(jsonPath("$.to").value(to.toString()))
        .andExpect(jsonPath("$.totalExpectedMeals").value(21))
        .andExpect(jsonPath("$.totalRecordedMeals").value(18))
        .andExpect(jsonPath("$.adherencePercentage").value(85.7));

    verify(adherenceReportService).getAdherence(patientId, from, to);
  }

  @Test
  @WithMockUser
  void shouldReturnNutritionComparisonWhenPatientAccessesOwnData() throws Exception {

    Long patientId = 1L;
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 7);

    NutritionComparisonReportDTO response = new NutritionComparisonReportDTO(from, to, List.of());

    when(dailyRecordService.getNutritionComparison(patientId, from, to)).thenReturn(response);

    mvc.perform(
            get("/api/patients/{patientId}/daily-records/nutrition-comparison", patientId)
                .param("from", from.toString())
                .param("to", to.toString())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.from").value(from.toString()))
        .andExpect(jsonPath("$.to").value(to.toString()));

    verify(dailyRecordService).getNutritionComparison(patientId, from, to);
  }
}
