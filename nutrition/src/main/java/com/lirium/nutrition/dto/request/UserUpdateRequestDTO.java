package com.lirium.nutrition.dto.request;

import java.time.LocalDate;

public record UserUpdateRequestDTO(
        String firstName,
        String lastName,
        LocalDate birthDate
) {}