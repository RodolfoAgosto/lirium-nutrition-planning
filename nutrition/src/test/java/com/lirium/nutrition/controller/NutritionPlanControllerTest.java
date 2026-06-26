package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.service.NutritionPlanGenerator;
import com.lirium.nutrition.service.NutritionPlanService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class NutritionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NutritionPlanGenerator nutritionPlanGenerator;

    @MockBean
    private NutritionPlanService nutritionPlanService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGenerateNutritionPlan() throws Exception {

        NutritionPlanDetailDTO response = mock(NutritionPlanDetailDTO.class);

        when(nutritionPlanGenerator.generate(1L))
                .thenReturn(response);

        mockMvc.perform(post("/api/nutrition-plans/generate/1"))
                .andExpect(status().isCreated());

        verify(nutritionPlanGenerator)
                .generate(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCompleteNutritionPlan() throws Exception {

        CompleteNutritionPlanRequestDTO request =
                mock(CompleteNutritionPlanRequestDTO.class);

        NutritionPlanDetailDTO response =
                mock(NutritionPlanDetailDTO.class);

        when(nutritionPlanService.complete(
                eq(1L),
                any(CompleteNutritionPlanRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/nutrition-plans/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(nutritionPlanService)
                .complete(
                        eq(1L),
                        any(CompleteNutritionPlanRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldActivateNutritionPlan() throws Exception {

        when(nutritionPlanService.activatePlan(anyLong()))
                .thenReturn(mock(NutritionPlanDetailDTO.class));

        mockMvc.perform(patch("/api/nutrition-plans/1/activate"))
                .andExpect(status().isNoContent());

        verify(nutritionPlanService)
                .activatePlan(1L);
    }

    @Test
    void activate_shouldReturn204() throws Exception {

        when(nutritionPlanService.activatePlan(1L))
                .thenReturn(mock(NutritionPlanDetailDTO.class));

        mockMvc.perform(
                        patch("/api/nutrition-plans/1/activate"))
                .andExpect(status().isNoContent());

        verify(nutritionPlanService).activatePlan(1L);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGenerateNutritionPlanFromTemplate() throws Exception {

        NutritionPlanDetailDTO response =
                mock(NutritionPlanDetailDTO.class);

        when(nutritionPlanGenerator.generateFromTemplate(1L, 2L))
                .thenReturn(response);

        mockMvc.perform(post("/api/nutrition-plans/generate-from-template/1/2"))
                .andExpect(status().isOk());

        verify(nutritionPlanGenerator)
                .generateFromTemplate(1L, 2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindNutritionPlanById() throws Exception {

        NutritionPlanDetailDTO response =
                mock(NutritionPlanDetailDTO.class);

        when(nutritionPlanService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/nutrition-plans/1"))
                .andExpect(status().isOk());

        verify(nutritionPlanService)
                .findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindNutritionPlansByPatient() throws Exception {

        List<NutritionPlanSummaryDTO> response = List.of(
                new NutritionPlanSummaryDTO(
                        1L,
                        "Weight Loss Plan",
                        PlanStatus.ACTIVE,
                        GoalType.WEIGHT_LOSS,
                        2000,
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                )
        );

        when(nutritionPlanService.findByPatient(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/nutrition-plans/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Weight Loss Plan"));

        verify(nutritionPlanService)
                .findByPatient(1L);
    }
}