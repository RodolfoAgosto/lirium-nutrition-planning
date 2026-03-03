package com.lirium.nutrition.dto.request;

import java.time.LocalDate;

public record UpdateUserRequestDTO(
        String firstName,
        String lastName,
        LocalDate birthDate
) {}