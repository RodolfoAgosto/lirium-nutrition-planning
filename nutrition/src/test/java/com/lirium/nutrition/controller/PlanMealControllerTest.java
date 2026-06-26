package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateFoodRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanMealController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanMealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanMealService planMealService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void getById_shouldReturnMeal() throws Exception {

        when(planMealService.getById(1L))
                .thenReturn(mealResponse());

        mockMvc.perform(get("/api/plan-meals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"))
                .andExpect(jsonPath("$.dailyPlanId").value(1));

        verify(planMealService).getById(1L);
    }

    @Test
    void getByPlanDay_shouldReturnMeals() throws Exception {

        when(planMealService.getByPlanDay(1L))
                .thenReturn(List.of(mealSummary()));

        mockMvc.perform(get("/api/plan-meals/day/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("BREAKFAST"));

        verify(planMealService).getByPlanDay(1L);
    }

    @Test
    void create_shouldReturnCreatedMeal() throws Exception {

        PlanMealCreateRequestDTO request = createRequest();

        when(planMealService.create(any(PlanMealCreateRequestDTO.class)))
                .thenReturn(mealResponse());

        mockMvc.perform(post("/api/plan-meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"))
                .andExpect(jsonPath("$.dailyPlanId").value(1));

        verify(planMealService).create(any(PlanMealCreateRequestDTO.class));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {

        doNothing().when(planMealService).delete(1L);

        mockMvc.perform(delete("/api/plan-meals/1"))
                .andExpect(status().isOk());

        verify(planMealService).delete(1L);
    }

    @Test
    void addPortion_shouldReturnUpdatedMeal() throws Exception {

        FoodPortionAddRequestDTO request =
                new FoodPortionAddRequestDTO(
                        100L,
                        150.0,
                        MeasureUnit.GRAM
                );

        when(planMealService.addPortion(eq(1L), any(FoodPortionAddRequestDTO.class)))
                .thenReturn(mealResponse());

        mockMvc.perform(post("/api/plan-meals/1/portions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"));

        verify(planMealService)
                .addPortion(eq(1L), any(FoodPortionAddRequestDTO.class));
    }

    @Test
    void removePortion_shouldReturnUpdatedMeal() throws Exception {

        when(planMealService.removePortion(1L, 2L))
                .thenReturn(mealResponse());

        mockMvc.perform(delete("/api/plan-meals/1/portions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"));

        verify(planMealService)
                .removePortion(1L, 2L);
    }

    @Test
    void updatePortion_shouldReturnUpdatedMeal() throws Exception {

        PlanFoodPortionUpdateFoodRequestDTO request =
                new PlanFoodPortionUpdateFoodRequestDTO(
                        100L,
                        200.0
                );

        when(planMealService.updatePortion(
                eq(1L),
                eq(2L),
                any(PlanFoodPortionUpdateFoodRequestDTO.class)
        )).thenReturn(mealResponse());

        mockMvc.perform(
                        patch("/api/plan-meals/1/portions/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BREAKFAST"));

        verify(planMealService)
                .updatePortion(
                        eq(1L),
                        eq(2L),
                        any(PlanFoodPortionUpdateFoodRequestDTO.class)
                );
    }

    private PlanMealSummaryDTO mealSummary() {
        return new PlanMealSummaryDTO(
                1L,
                "BREAKFAST"
        );
    }

    private PlanMealResponseDTO mealResponse() {
        return new PlanMealResponseDTO(
                1L,
                "BREAKFAST",
                1L,
                List.of()
        );
    }

    private PlanMealCreateRequestDTO createRequest() {
        return new PlanMealCreateRequestDTO(
                "BREAKFAST",
                1L,
                List.of(1L, 2L)
        );
    }

}