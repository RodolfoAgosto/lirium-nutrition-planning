package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.service.NutritionPlanTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionPlanTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class NutritionPlanTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NutritionPlanTemplateService nutritionPlanTemplateService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void shouldGetAllTemplates() throws Exception {

        List<NutritionPlanTemplateSummaryDTO> response = List.of(
                new NutritionPlanTemplateSummaryDTO(
                        1L,
                        "Weight Loss",
                        GoalType.WEIGHT_LOSS
                )
        );

        when(nutritionPlanTemplateService.getAll()).thenReturn(response);

        mockMvc.perform(get("/api/nutrition-plan-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Weight Loss"));

        verify(nutritionPlanTemplateService).getAll();
    }

    @Test
    void shouldGetTemplateById() throws Exception {

        NutritionPlanTemplateResponseDTO response =
                new NutritionPlanTemplateResponseDTO(
                        1L,
                        "Weight Loss",
                        "Template for fat loss",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        20,
                        Set.of()
                );

        when(nutritionPlanTemplateService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/nutrition-plan-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weight Loss"));

        verify(nutritionPlanTemplateService).getById(1L);
    }

    @Test
    void shouldCreateTemplate() throws Exception {

        NutritionPlanTemplateCreateRequestDTO request =
                new NutritionPlanTemplateCreateRequestDTO(
                        "Weight Loss",
                        "Template for fat loss",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        20,
                        Set.of()
                );

        NutritionPlanTemplateResponseDTO response =
                new NutritionPlanTemplateResponseDTO(
                        1L,
                        "Weight Loss",
                        "Template for fat loss",
                        GoalType.WEIGHT_LOSS,
                        40,
                        40,
                        20,
                        Set.of()
                );

        when(nutritionPlanTemplateService.create(any(NutritionPlanTemplateCreateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/nutrition-plan-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weight Loss"));

        verify(nutritionPlanTemplateService)
                .create(any(NutritionPlanTemplateCreateRequestDTO.class));
    }

    @Test
    void shouldDeleteTemplate() throws Exception {

        doNothing().when(nutritionPlanTemplateService).delete(1L);

        mockMvc.perform(delete("/api/nutrition-plan-templates/1"))
                .andExpect(status().isNoContent());

        verify(nutritionPlanTemplateService).delete(1L);
    }

    @Test
    void shouldUpdateTemplate() throws Exception {

        NutritionPlanTemplateUpdateRequestDTO request =
                new NutritionPlanTemplateUpdateRequestDTO(
                        "Updated Template",
                        "Updated Description",
                        GoalType.WEIGHT_LOSS,
                        35,
                        45,
                        20,
                        Set.of()
                );

        NutritionPlanTemplateResponseDTO response =
                new NutritionPlanTemplateResponseDTO(
                        1L,
                        "Updated Template",
                        "Updated Description",
                        GoalType.WEIGHT_LOSS,
                        35,
                        45,
                        20,
                        Set.of()
                );

        when(nutritionPlanTemplateService.update(eq(1L),
                any(NutritionPlanTemplateUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/nutrition-plan-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Updated Template"));

        verify(nutritionPlanTemplateService)
                .update(eq(1L),
                        any(NutritionPlanTemplateUpdateRequestDTO.class));
    }


}