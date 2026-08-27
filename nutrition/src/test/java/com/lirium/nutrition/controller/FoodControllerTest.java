package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.service.FoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

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

        FoodCreateRequestDTO dto = new FoodCreateRequestDTO(
                "Chicken Breast", 165, 31, 0, 3,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH),
                Set.of(FoodTag.MEAT)
        );

        FoodSummaryDTO response =
                new FoodSummaryDTO(1L, "Chicken Breast");

        when(foodService.create(any(FoodCreateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chicken Breast"));

        verify(foodService).create(any(FoodCreateRequestDTO.class));
    }

    @Test
    void shouldUpdateFood() throws Exception {

        FoodUpdateRequestDTO dto = new FoodUpdateRequestDTO("Chicken", 100, 20, 0, 5, Set.of(FoodTag.GLUTEN));

        FoodSummaryDTO response =
                new FoodSummaryDTO(1L, "Updated Chicken");

        when(foodService.update(eq(1L), any(FoodUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/foods/1") // <-- Cambiado de put(...) a patch(...)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chicken"));

        verify(foodService)
                .update(eq(1L), any(FoodUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteFood() throws Exception {
        // Given
        Long foodId = 1L;
        doNothing().when(foodService).deleteById(foodId);

        // When & Then (Sino podés remover doNothing() ya que es void por defecto en mocks)
        mockMvc.perform(delete("/api/foods/{id}", foodId) // <-- Corregido sin /v1
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(foodService).deleteById(foodId);
    }

}