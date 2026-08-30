package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserUpdateRequestDTO(
    @NotBlank(message = "First name is required") @Size(max = 50) String firstName,
    @NotBlank(message = "Last name is required") @Size(max = 50) String lastName,
    @Past(message = "Birth date must be in the past") LocalDate birthDate) {}
