package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateFoodRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateQuantityRequestDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.service.PlanFoodPortionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanFoodPortionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanFoodPortionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanFoodPortionService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void shouldGetFoodPortionsByMeal() throws Exception {

        List<PlanFoodPortionResponseDTO> response = List.of(
                new PlanFoodPortionResponseDTO(
                        1L,
                        10L,
                        100L,
                        "Apple",
                        150.0,
                        MeasureUnit.GRAM
                ),
                new PlanFoodPortionResponseDTO(
                        2L,
                        10L,
                        101L,
                        "Banana",
                        200.0,
                        MeasureUnit.GRAM
                )
        );

        when(service.getByPlanMeal(10L))
                .thenReturn(response);

        mockMvc.perform(get("/api/plan-food-portions/meal/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].foodName").value("Apple"))
                .andExpect(jsonPath("$[0].quantity").value(150.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].foodName").value("Banana"))
                .andExpect(jsonPath("$[1].quantity").value(200.0));

        verify(service).getByPlanMeal(10L);
    }

    @Test
    void shouldGetFoodPortionById() throws Exception {

        PlanFoodPortionResponseDTO response =
                new PlanFoodPortionResponseDTO(
                        1L,
                        10L,
                        100L,
                        "Apple",
                        150.0,
                        MeasureUnit.GRAM
                );

        when(service.getById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/plan-food-portions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mealId").value(10))
                .andExpect(jsonPath("$.foodId").value(100))
                .andExpect(jsonPath("$.foodName").value("Apple"))
                .andExpect(jsonPath("$.quantity").value(150.0))
                .andExpect(jsonPath("$.unit").value("GRAM"));

        verify(service).getById(1L);
    }
}