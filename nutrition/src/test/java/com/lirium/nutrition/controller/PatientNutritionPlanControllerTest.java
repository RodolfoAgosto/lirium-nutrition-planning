package com.lirium.nutrition.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.TokenBlacklistService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.service.NutritionPlanService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientNutritionPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientNutritionPlanControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private NutritionPlanService nutritionPlanService;

  @MockBean private JwtService jwtService;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private UserDetailsServiceImpl userDetailsServiceImpl;

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldFindNutritionPlansByPatient() throws Exception {

    List<NutritionPlanSummaryDTO> response =
        List.of(
            new NutritionPlanSummaryDTO(
                1L,
                "Weight Loss Plan",
                PlanStatus.ACTIVE,
                GoalType.WEIGHT_LOSS,
                2000,
                LocalDate.now(),
                LocalDate.now().plusDays(30)));

    when(nutritionPlanService.findByPatient(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/patients/1/nutrition-plans"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Weight Loss Plan"));

    verify(nutritionPlanService).findByPatient(1L);
  }
}
