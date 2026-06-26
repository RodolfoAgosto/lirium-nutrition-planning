package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FoodController.class)
@AutoConfigureMockMvc(addFilters = false)
class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FoodService foodService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void shouldFindAllFoods() throws Exception {

        Set<FoodSummaryDTO> response = Set.of(
                new FoodSummaryDTO(1L, "Chicken Breast")
        );

        when(foodService.findAll()).thenReturn(response);

        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk());

        verify(foodService).findAll();
    }

    @Test
    void shouldFindFoodById() throws Exception {

        FoodResponseDTO response = new FoodResponseDTO(
                1L,
                "Chicken Breast",
                165,
                31,
                0,
                4,
                FoodCategory.PROTEIN,
                Set.of(),
                Set.of()
        );

        when(foodService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/foods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chicken Breast"));

        verify(foodService).findById(1L);
    }

    @Test
    void shouldCreateFood() throws Exception {

        FoodCreateRequestDTO request = new FoodCreateRequestDTO(
                "Chicken Breast",
                165,
                31,
                0,
                4,
                FoodCategory.PROTEIN,
                Set.of(),
                Set.of("HIGH_PROTEIN")
        );

        FoodSummaryDTO response =
                new FoodSummaryDTO(1L, "Chicken Breast");

        when(foodService.create(any(FoodCreateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chicken Breast"));

        verify(foodService).create(any(FoodCreateRequestDTO.class));
    }

    @Test
    void shouldUpdateFood() throws Exception {

        FoodUpdateRequestDTO request = new FoodUpdateRequestDTO(
                "Updated Chicken",
                180,
                35,
                0,
                5,
                Set.of("LEAN")
        );

        FoodSummaryDTO response =
                new FoodSummaryDTO(1L, "Updated Chicken");

        when(foodService.update(eq(1L), any(FoodUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/foods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chicken"));

        verify(foodService)
                .update(eq(1L), any(FoodUpdateRequestDTO.class));
    }

    @Test
    void shouldDeleteFood() throws Exception {

        doNothing().when(foodService).deleteById(1L);

        mockMvc.perform(delete("/api/foods/1"))
                .andExpect(status().isOk());

        verify(foodService).deleteById(1L);
    }

}