package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.service.RestrictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestrictionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestrictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestrictionService restrictionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // findAll
    @Test
    void shouldFindAllRestrictions() throws Exception {

        Set<RestrictionSummaryDTO> response = Set.of(
                new RestrictionSummaryDTO(
                        1L,
                        "GLUTEN",
                        "Gluten",
                        RestrictionCategory.DIETARY
                )
        );

        when(restrictionService.findAll()).thenReturn(response);

        mockMvc.perform(get("/api/restrictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(restrictionService).findAll();
    }

    // create
    @Test
    void shouldCreateRestriction() throws Exception {

        RestrictionCreateRequestDTO request =
                new RestrictionCreateRequestDTO(
                        "GLUTEN",
                        "Gluten",
                        "ALLERGY",
                        "Contains gluten"
                );

        RestrictionSummaryDTO response =
                new RestrictionSummaryDTO(
                        1L,
                        "GLUTEN",
                        "Gluten",
                        RestrictionCategory.DIETARY
                );

        when(restrictionService.create(any(RestrictionCreateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/restrictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("GLUTEN"))
                .andExpect(jsonPath("$.name").value("Gluten"));

        verify(restrictionService)
                .create(any(RestrictionCreateRequestDTO.class));
    }

    // finfById
    @Test
    void shouldFindRestrictionById() throws Exception {

        RestrictionResponseDTO response =
                new RestrictionResponseDTO(
                        1L,
                        "GLUTEN",
                        "Gluten",
                        "ALLERGY",
                        "Contains gluten"
                );

        when(restrictionService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/restrictions/1"))
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
                        "GLUTEN",
                        "Updated Gluten",
                        "ALLERGY",
                        "Updated description"
                );

        RestrictionSummaryDTO response =
                new RestrictionSummaryDTO(
                        1L,
                        "GLUTEN",
                        "Updated Gluten",
                        RestrictionCategory.DIETARY
                );

        when(restrictionService.update(eq(1L), any(RestrictionCatalogUpdateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/restrictions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Gluten"));

        verify(restrictionService)
                .update(eq(1L), any(RestrictionCatalogUpdateDTO.class));
    }


}