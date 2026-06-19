package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.service.AdherenceReportService;
import com.lirium.nutrition.service.DailyRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DailyRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class DailyRecordControllerTest {

    @MockBean
    private DailyRecordService dailyRecordService;

    @MockBean
    private AdherenceReportService adherenceReportService;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    // getOrCreateToday

    @Test
    void shouldReturnTodayRecordWhenPatientAccessesOwnRecord() throws Exception {

        // Given
        Long patiendId = 1L;
        LocalDate today = LocalDate.now();
        DailyRecordResponseDTO dailyRecordResponseDTO = new DailyRecordResponseDTO(1L, today,List.of());

        // When
        when(dailyRecordService.getOrCreateToday(1L)).thenReturn(dailyRecordResponseDTO);

        // Then
        mvc.perform(get("/api/daily-records/today/"+ patiendId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").value(today.toString()));

        verify(dailyRecordService).getOrCreateToday(1L);

    }

    // getById
    @Test
    @WithMockUser
    void shouldReturnDailyRecordById() throws Exception {

        LocalDate today = LocalDate.now();
        DailyRecordResponseDTO response = new DailyRecordResponseDTO(
                1L,
                today,
                List.of()
        );

        when(dailyRecordService.getById(1L))
                .thenReturn(response);

        mvc.perform(get("/api/daily-records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").value(today.toString()))
                .andExpect(jsonPath("$.meals").isArray());

        verify(dailyRecordService).getById(1L);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenDailyRecordDoesNotExist() throws Exception {

        when(dailyRecordService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Daily record not found", 999L));

        mvc.perform(get("/api/daily-records/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(dailyRecordService).getById(999L);
    }

    // getByPatient
    @Test
    @WithMockUser(roles = "PATIENT")
    void shouldReturnPatientRecordsWhenPatientAccessesOwnRecords() throws Exception {

        List<DailyRecordResponseDTO> records = List.of(
                new DailyRecordResponseDTO(
                        1L,
                        LocalDate.of(2025, 1, 15),
                        List.of()
                ),
                new DailyRecordResponseDTO(
                        2L,
                        LocalDate.of(2025, 1, 16),
                        List.of()
                )
        );

        when(dailyRecordService.getByPatient(1L))
                .thenReturn(records);

        mvc.perform(get("/api/daily-records/patient/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(dailyRecordService).getByPatient(1L);
    }

    // updateMeal
    @Test
    @WithMockUser
    void shouldUpdateMealSuccessfully() throws Exception {

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("Updated notes");

        MealRecordResponseDTO response =
                new MealRecordResponseDTO(
                        1L,
                        MealType.BREAKFAST,
                        false,
                        "Updated notes",
                        LocalDateTime.of(2025, 1, 15, 10, 30),
                        List.of()
                );

        when(dailyRecordService.updateMeal(eq(1L), any(MealRecordUpdateRequestDTO.class)))
                .thenReturn(response);

        mvc.perform(patch("/api/daily-records/meals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"))
                .andExpect(jsonPath("$.overridden").value(false))
                .andExpect(jsonPath("$.notes").value("Updated notes"))
                .andExpect(jsonPath("$.portions").isArray());

        verify(dailyRecordService).updateMeal(eq(1L), any(MealRecordUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenMealRecordDoesNotExist() throws Exception {

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("Updated notes");

        when(dailyRecordService.updateMeal(eq(999L), any(MealRecordUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Meal record not found", 999L));

        mvc.perform(patch("/api/daily-records/meals/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(dailyRecordService).updateMeal(eq(999L), any(MealRecordUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("");

        mvc.perform(patch("/api/daily-records/meals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dailyRecordService);
    }

    // addPortion
    @Test
    @WithMockUser
    void shouldAddPortionSuccessfully() throws Exception {

        AddFoodPortionRequestDTO request =
                new AddFoodPortionRequestDTO(
                        10L,
                        2.0,
                        MeasureUnit.GRAM
                );

        MealRecordResponseDTO response =
                new MealRecordResponseDTO(
                        1L,
                        MealType.LUNCH,
                        false,
                        "notes",
                        LocalDateTime.of(2025, 1, 15, 12, 0),
                        List.of()
                );

        when(dailyRecordService.addPortion(eq(1L), any(AddFoodPortionRequestDTO.class)))
                .thenReturn(response);

        mvc.perform(post("/api/daily-records/meals/1/portions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("LUNCH"))
                .andExpect(jsonPath("$.overridden").value(false));

        verify(dailyRecordService).addPortion(eq(1L), any(AddFoodPortionRequestDTO.class));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenMealRecordDoesNotExistWhenAddingPortion() throws Exception {

        AddFoodPortionRequestDTO request =
                new AddFoodPortionRequestDTO(
                        10L,
                        2.0,
                        MeasureUnit.GRAM
                );

        when(dailyRecordService.addPortion(eq(999L), any(AddFoodPortionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Meal record not found", 999L));

        mvc.perform(post("/api/daily-records/meals/999/portions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(dailyRecordService).addPortion(eq(999L), any(AddFoodPortionRequestDTO.class));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenPortionRequestIsInvalid() throws Exception {

        AddFoodPortionRequestDTO request =
                new AddFoodPortionRequestDTO(
                        null,
                        -1.0,
                        null
                );

        mvc.perform(post("/api/daily-records/meals/1/portions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dailyRecordService);
    }

    // removePortion
    @Test
    @WithMockUser
    void shouldRemovePortionSuccessfully() throws Exception {

        Long dailyRecordId = 1L;
        Long mealRecordId = 10L;
        Long portionId = 100L;

        mvc.perform(delete("/api/daily-records/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}",
                        dailyRecordId, mealRecordId, portionId))
                .andExpect(status().isNoContent());

        verify(dailyRecordService).removePortion(dailyRecordId, mealRecordId, portionId);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenPortionDoesNotExist() throws Exception {

        Long dailyRecordId = 1L;
        Long mealRecordId = 10L;
        Long portionId = 999L;

        doThrow(new ResourceNotFoundException("Portion not found", 999L))
                .when(dailyRecordService)
                .removePortion(dailyRecordId, mealRecordId, portionId);

        mvc.perform(delete("/api/daily-records/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}",
                        dailyRecordId, mealRecordId, portionId))
                .andExpect(status().isNotFound());

        verify(dailyRecordService).removePortion(dailyRecordId, mealRecordId, portionId);
    }

    // adherence
    @Test
    @WithMockUser
    void shouldReturnAdherenceReportWhenPatientAccessesOwnData() throws Exception {

        Long patientId = 1L;
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 7);

        AdherenceReportDTO response =
                new AdherenceReportDTO(
                        from,
                        to,
                        21,
                        18,
                        85.7,
                        List.of()
                );

        when(adherenceReportService.getAdherence(patientId, from, to))
                .thenReturn(response);

        mvc.perform(get("/api/daily-records/patient/{patientId}/adherence", patientId)
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

        NutritionComparisonReportDTO response =
                new NutritionComparisonReportDTO(
                        from,
                        to,
                        List.of()
                );

        when(dailyRecordService.getNutritionComparison(patientId, from, to))
                .thenReturn(response);

        mvc.perform(get("/api/daily-records/patient/{patientId}/nutrition-comparison", patientId)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value(from.toString()))
                .andExpect(jsonPath("$.to").value(to.toString()));

        verify(dailyRecordService)
                .getNutritionComparison(patientId, from, to);
    }


}