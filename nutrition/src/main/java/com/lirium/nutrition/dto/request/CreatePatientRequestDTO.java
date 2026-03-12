package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/*
 * DTO used when a nutritionist registers a new patient.
 *
 * The nutritionist creates the user account and the system
 * automatically creates the associated PatientProfile.
 *
 * The password is not required at this stage because the
 * patient will set it later through an invitation or activation flow.
 */
public record CreatePatientRequestDTO(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50)
        String lastName,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @Size(max = 8)
        @Pattern(regexp = "\\d{7,8}", message = "DNI must contain 7 or 8 digits")
        String dni

) {}