package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String dni,
        Boolean emailValidated,
        Boolean enabled
) {}