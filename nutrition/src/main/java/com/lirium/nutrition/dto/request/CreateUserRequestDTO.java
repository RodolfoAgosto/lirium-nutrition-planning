package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/*
 * DTO used when a patient registers themselves.
 * Only email and password are strictly required for account creation.
 */
public record CreateUserRequestDTO(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must have between 8 and 100 characters")
        String password,
    @NotBlank(message = "First name is required") @Size(max = 50) String firstName,
    @NotBlank(message = "Last name is required") @Size(max = 50) String lastName,
    @Past(message = "Birth date must be in the past") LocalDate birthDate,
    @Pattern(regexp = "\\d{7,8}", message = "DNI must contain 7 or 8 digits") String dni) {}
