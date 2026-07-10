package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.repository.RestrictionRepository;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.model.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class RestrictionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private RestrictionRepository restrictionRepository;

    private String adminToken;

    private Restriction restriction;


    @BeforeEach
    void setup() {

        restrictionRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin",
                "Test",
                Role.ADMIN
        ));

        adminToken = "Bearer " + jwtService.generateToken(admin);

        restriction = restrictionRepository.save(
                Restriction.builder()
                        .code("GLUTEN")
                        .name("Gluten")
                        .description("Gluten restriction")
                        .category(RestrictionCategory.PATHOLOGICAL)
                        .build()
        );

    }


    @Test
    @DisplayName("Debe obtener todas las restricciones")
    void shouldGetAllRestrictions() throws Exception {

        mockMvc.perform(get("/api/restrictions")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("GLUTEN"));
    }


    @Test
    @DisplayName("Debe obtener restricción por id")
    void shouldGetRestrictionById() throws Exception {

        mockMvc.perform(get("/api/restrictions/{id}", restriction.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restriction.getId()))
                .andExpect(jsonPath("$.code").value("GLUTEN"));
    }


    @Test
    @DisplayName("Debe crear una restricción")
    void shouldCreateRestriction() throws Exception {

        mockMvc.perform(post("/api/restrictions")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "LACTOSE",
                                    "name": "Lactose",
                                    "category": "INTOLERANCES",
                                    "description": "Lactose intolerance"
                                }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("LACTOSE"))
                .andExpect(jsonPath("$.name").value("Lactose"));
    }


    @Test
    @DisplayName("Debe actualizar una restricción")
    void shouldUpdateRestriction() throws Exception {

        mockMvc.perform(put("/api/restrictions/{id}", restriction.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                     "code": "GLUTEN_UPDATED",
                                     "name": "Gluten Updated",
                                     "category": "DIETARY",
                                     "description": "Updated description"
                                 }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GLUTEN_UPDATED"))
                .andExpect(jsonPath("$.name").value("Gluten Updated"));
    }


    @Test
    @DisplayName("Debe devolver 404 si no existe la restricción")
    void shouldReturn404WhenRestrictionDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/restrictions/{id}", 99999L)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Debe rechazar creación con categoría inválida")
    void shouldRejectInvalidCategory() throws Exception {

        mockMvc.perform(post("/api/restrictions")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "code": "TEST",
                            "name": "Test",
                            "category": "INVALID",
                            "description": "Invalid category"
                        }
                        """))
                       .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Usuario no autenticado no puede crear restricción")
    void shouldRejectCreateWithoutAuthentication() throws Exception {

        mockMvc.perform(post("/api/restrictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "code": "TEST",
                            "name": "Test",
                            "category": "ALLERGY",
                            "description": "Test"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

}