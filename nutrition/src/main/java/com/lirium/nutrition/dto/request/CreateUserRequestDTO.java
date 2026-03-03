package com.lirium.nutrition.dto.request;

import java.time.LocalDate;

public record CreateUserRequestDTO(
        String email,
        String password,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String dni
) {}