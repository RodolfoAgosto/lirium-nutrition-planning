package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MacroDistributorImplTest {

    @Test
    void shouldDistributeMacrosForMaleSedentaryPatient() {

        // Given
        User user = new User(
                "john@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                Sex.MALE,
                ActivityLevel.SEDENTARY,
                Weight.of(80_000),
                Height.of(180),
                null,
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        MacroDistributorImpl distributor = new MacroDistributorImpl();

        // When
        MacroDistribution result =
                distributor.distribute(
                        patient,
                        new Calories(2000)
                );

        // Then
        assertAll(
                () -> assertEquals(64, result.proteinGrams()),
                () -> assertEquals(286, result.carbGrams()),
                () -> assertEquals(67, result.fatGrams())
        );

    }

    @Test
    void shouldDistributeMacrosForFemaleSedentaryPatient() {

        // Given
        User user = new User(
                "sarah@test.com",
                "hash",
                "sarah",
                "Doe",
                Role.PATIENT
        );

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                Sex.FEMALE,
                ActivityLevel.MODERATE,
                Weight.of(80_000),
                Height.of(180),
                null,
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        MacroDistributorImpl distributor = new MacroDistributorImpl();

        // When
        MacroDistribution result =
                distributor.distribute(
                        patient,
                        new Calories(2000)
                );

        // Then
        assertAll(
                () -> assertEquals(88, result.proteinGrams()),
                () -> assertEquals(252, result.carbGrams()),
                () -> assertEquals(71, result.fatGrams())
        );

    }

    @Test
    void shouldDistributeMacrosFromTemplate() {

        // Given
        NutritionPlanTemplate template =
                mock(NutritionPlanTemplate.class);

        when(template.getProteinPercentage())
                .thenReturn(30);

        when(template.getCarbPercentage())
                .thenReturn(40);

        when(template.getFatPercentage())
                .thenReturn(30);

        MacroDistributorImpl distributor = new MacroDistributorImpl();

        // When

        MacroDistribution result =
                distributor.distributeFromTemplate(
                        new Calories(2000),
                        template
                );

        // Then
        assertAll(
                () -> assertEquals(150, result.proteinGrams()),
                () -> assertEquals(200, result.carbGrams()),
                () -> assertEquals(66, result.fatGrams())
        );
    }

    @Test
    void shouldCalculateTemplateMacrosWithRoundingLoss() {

        // Given
        NutritionPlanTemplate template =
                mock(NutritionPlanTemplate.class);

        when(template.getProteinPercentage())
                .thenReturn(30);

        when(template.getCarbPercentage())
                .thenReturn(40);

        when(template.getFatPercentage())
                .thenReturn(30);

        MacroDistributorImpl distributor = new MacroDistributorImpl();

        // When
        MacroDistribution result =
                distributor.distributeFromTemplate(
                        new Calories(2150),
                        template
                );

        // Then
        assertAll(
                () -> assertEquals(161, result.proteinGrams()),
                () -> assertEquals(215, result.carbGrams()),
                () -> assertEquals(71, result.fatGrams())
        );
    }

    @Test
    void shouldDistributeMacrosForHighlyActiveMalePatient() {

        // Given
        User user = new User(
                "athlete@test.com",
                "hash",
                "Mike",
                "Doe",
                Role.PATIENT
        );

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                Sex.MALE,
                ActivityLevel.VERY_ACTIVE,
                Weight.of(80_000),
                Height.of(180),
                null,
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        MacroDistributorImpl distributor = new MacroDistributorImpl();

        // When
        MacroDistribution result =
                distributor.distribute(
                        patient,
                        new Calories(3000)
                );

        // Then
        assertAll(
                () -> assertTrue(result.proteinGrams() > 0),
                () -> assertTrue(result.carbGrams() > 0),
                () -> assertTrue(result.fatGrams() > 0)
        );
    }

}
