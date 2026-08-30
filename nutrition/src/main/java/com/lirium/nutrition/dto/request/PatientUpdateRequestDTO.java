package com.lirium.nutrition.dto.request;

import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.enums.Sex;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record PatientUpdateRequestDTO(
    @Schema(example = "Ana Juana")
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,
    @Schema(example = "López")
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,
    @Schema(example = "ana.lopez@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
    @Schema(example = "38123456")
        @Pattern(regexp = "\\d{7,8}", message = "DNI must contain 7 or 8 digits")
        String dni,
    @Schema(example = "FEMALE") Sex sex,
    @Schema(example = "true") Boolean enabled,
    @Schema(example = "1995-05-20") @Past(message = "Birth date must be in the past")
        LocalDate birthDate,
    @Schema(example = "165")
        @Min(value = 80, message = "Height must be greater than or equal to 80 cm")
        @Max(value = 250, message = "Height must be less than or equal to 250 cm")
        Integer height,
    @Schema(example = "50000")
        @Min(value = 2000, message = "Weight must be greater than or equal to 2000 g")
        @Max(value = 250000, message = "Weight must be less than or equal to 250000 g")
        Integer weight,
    @Schema(example = "MODERATE") ActivityLevel activityLevel,
    @Schema(example = "WEIGHT_LOSS") GoalType goal,
    @Schema(example = "Paciente con intolerancia al gluten.") String medicalNotes,
    @Schema(example = "[{\"code\": \"GLUTEN_FREE\"}]") Set<RestrictionUpdateDTO> restrictions,
    @ArraySchema(schema = @Schema(example = "PREGNANCY"))
        List<PhysiologicalCondition> physiologicalConditions) {}
