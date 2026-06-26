package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.dto.response.UserResponseDTO;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import com.lirium.nutrition.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // registerUser - OK (201)
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "test@mail.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678"
        );

        UserResponseDTO responseDTO = new UserResponseDTO(
                1L,
                "test@mail.com",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678",
                false,
                true
        );

        when(userService.registerUser(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@mail.com"));

        verify(userService).registerUser(any());
    }

    // registerPatient
    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {

        CreatePatientRequestDTO request = new CreatePatientRequestDTO(
                "patient@mail.com",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678"
        );

        UserResponseDTO response = new UserResponseDTO(
                1L,
                "patient@mail.com",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678",
                false,
                true
        );

        when(userService.registerPatient(any(CreatePatientRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("patient@mail.com"));

        verify(userService).registerPatient(any(CreatePatientRequestDTO.class));
    }


    // findById
    @Test
    void shouldFindById() throws Exception {

        UserResponseDTO responseDTO = new UserResponseDTO(
                1L, "test@mail.com", "John", "Doe",
                LocalDate.of(2000, 1, 1), "12345678",
                true, true
        );

        when(userService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).findById(1L);
    }

    // findAll
    @Test
    void shouldFindAllUsers() throws Exception {

        List<UserResponseDTO> list = List.of(
                new UserResponseDTO(
                        1L, "test@mail.com", "John", "Doe",
                        LocalDate.of(2000, 1, 1), "12345678",
                        true, true
                )
        );

        when(userService.findAll()).thenReturn(list);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(userService).findAll();
    }

    // updateBasicInfo
    @Test
    void shouldUpdateUser() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO("John", "Updated",
                LocalDate.of(2000, 1, 1));
        UserResponseDTO responseDTO = new UserResponseDTO(
                1L, "test@mail.com", "John", "Updated",
                LocalDate.of(2000, 1, 1), "12345678",
                true, true
        );

        when(userService.updateBasicInfo(eq(1L), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(userService).updateBasicInfo(eq(1L), any());
    }

    @Test
    void shouldValidateEmailSuccessfully() throws Exception {

        UserResponseDTO response = new UserResponseDTO(
                1L,
                "test@mail.com",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678",
                true,
                true
        );

        when(userService.validateEmail(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/users/1/validate-email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailValidated").value(true));

        verify(userService).validateEmail(1L);
    }

    // delete
    @Test
    void shouldDeleteUser() throws Exception {

        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteById(1L);
    }

    // 400 BAD REQUEST

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "invalid-email",
                "password123",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordTooShort() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "test@mail.com",
                "123",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDniInvalid() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "test@mail.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "ABC123"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBirthDateInFuture() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "test@mail.com",
                "password123",
                "John",
                "Doe",
                LocalDate.now().plusDays(1),
                "12345678"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEmailBlank() throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "",
                "password123",
                "John",
                "Doe",
                LocalDate.of(2000, 1, 1),
                "12345678"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}