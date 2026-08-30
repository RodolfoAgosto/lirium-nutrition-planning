package com.lirium.nutrition.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.RestrictionCatalogUpdateDTO;
import com.lirium.nutrition.dto.request.RestrictionCreateRequestDTO;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.service.RestrictionService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RestrictionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestrictionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private RestrictionService restrictionService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsServiceImpl userDetailsServiceImpl;

  // findAll
  @Test
  void shouldFindAllRestrictions() throws Exception {

    Set<RestrictionSummaryDTO> response =
        Set.of(new RestrictionSummaryDTO(1L, "GLUTEN", "Gluten", RestrictionCategory.DIETARY));

    when(restrictionService.findAll()).thenReturn(response);

    mockMvc
        .perform(get("/api/restrictions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(restrictionService).findAll();
  }

  // create
  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldCreateRestriction() throws Exception {

    RestrictionCreateRequestDTO request =
        new RestrictionCreateRequestDTO(
            "GLUTEN", "Gluten Free", "Contains gluten", RestrictionCategory.DIETARY);

    RestrictionSummaryDTO response =
        new RestrictionSummaryDTO(1L, "GLUTEN", "Gluten Free", RestrictionCategory.DIETARY);

    when(restrictionService.create(any(RestrictionCreateRequestDTO.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/restrictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.code").value("GLUTEN"))
        .andExpect(jsonPath("$.name").value("Gluten Free"));

    verify(restrictionService).create(any(RestrictionCreateRequestDTO.class));
  }

  // finfById
  @Test
  void shouldFindRestrictionById() throws Exception {

    RestrictionResponseDTO response =
        new RestrictionResponseDTO(1L, "GLUTEN", "Gluten", "ALLERGY", "Contains gluten");

    when(restrictionService.findById(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/restrictions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.code").value("GLUTEN"))
        .andExpect(jsonPath("$.name").value("Gluten"));

    verify(restrictionService).findById(1L);
  }

  // update
  @Test
  void shouldUpdateRestriction() throws Exception {

    RestrictionCatalogUpdateDTO request =
        new RestrictionCatalogUpdateDTO(
            "GLUTEN", "Updated Gluten", "ALLERGY", RestrictionCategory.INTOLERANCES);

    RestrictionSummaryDTO response =
        new RestrictionSummaryDTO(1L, "GLUTEN", "Updated Gluten", RestrictionCategory.DIETARY);

    when(restrictionService.update(eq(1L), any(RestrictionCatalogUpdateDTO.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/restrictions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Updated Gluten"));

    verify(restrictionService).update(eq(1L), any(RestrictionCatalogUpdateDTO.class));
  }
}
