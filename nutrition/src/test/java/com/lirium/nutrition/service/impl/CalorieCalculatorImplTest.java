package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CalorieCalculatorImplTest {

    private final CalorieCalculatorImpl calculator = new CalorieCalculatorImpl();

    @Test
    void shouldCalculateCaloriesForMalePatient() {

        User user = new User(
                "john@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setBirthDate(LocalDate.now().minusYears(30));

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

        CalorieCalculatorImpl calculator = new CalorieCalculatorImpl();

        Calories result = calculator.calculate(patient);

        assertEquals(2136, result.amount());

    }

    @Test
    void shouldCalculateCaloriesForFemalePatient() {

        User user = new User(
                "jane@test.com",
                "hash",
                "Jane",
                "Doe",
                Role.PATIENT
        );

        user.setBirthDate(LocalDate.now().minusYears(30));

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                Sex.FEMALE,
                ActivityLevel.SEDENTARY,
                Weight.of(80_000),
                Height.of(180),
                null,
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        CalorieCalculatorImpl calculator = new CalorieCalculatorImpl();

        Calories result = calculator.calculate(patient);

        assertEquals(1936, result.amount());

    }

    @Test
    void shouldApplyWeightLossGoal() {

        PatientProfile patient = createPatient(
                Sex.MALE,
                ActivityLevel.SEDENTARY,
                GoalType.WEIGHT_LOSS,
                List.of()
        );

        Calories result = calculator.calculate(patient);

        assertEquals(1708, result.amount());
    }

    @Test
    void shouldApplyMuscleGainGoal() {

        PatientProfile patient = createPatient(
                Sex.MALE,
                ActivityLevel.SEDENTARY,
                GoalType.MUSCLE_GAIN,
                List.of()
        );

        Calories result = calculator.calculate(patient);

        assertEquals(2456, result.amount());
    }

    @Test
    void shouldApplyPregnancyCondition() {

        PatientProfile patient = createPatient(
                Sex.FEMALE,
                ActivityLevel.SEDENTARY,
                GoalType.WEIGHT_MAINTENANCE,
                List.of(PhysiologicalCondition.PREGNANCY)
        );

        Calories result = calculator.calculate(patient);

        assertEquals(2236, result.amount());
    }

    @Test
    void shouldApplyLactationCondition() {

        PatientProfile patient = createPatient(
                Sex.FEMALE,
                ActivityLevel.SEDENTARY,
                GoalType.WEIGHT_MAINTENANCE,
                List.of(PhysiologicalCondition.LACTATION)
        );

        Calories result = calculator.calculate(patient);

        assertEquals(2436, result.amount());
    }

    @Test
    void shouldApplyMultiplePhysiologicalConditions() {

        PatientProfile patient = createPatient(
                Sex.FEMALE,
                ActivityLevel.SEDENTARY,
                GoalType.WEIGHT_MAINTENANCE,
                List.of(
                        PhysiologicalCondition.PREGNANCY,
                        PhysiologicalCondition.LACTATION
                )
        );

        Calories result = calculator.calculate(patient);

        assertEquals(2736, result.amount());
    }

    private PatientProfile createPatient(
            Sex sex,
            ActivityLevel activityLevel,
            GoalType goal,
            List<PhysiologicalCondition> conditions) {

        User user = new User(
                "test@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setBirthDate(LocalDate.now().minusYears(30));

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                sex,
                activityLevel,
                Weight.of(80_000),
                Height.of(180),
                null,
                Set.of(),
                conditions,
                goal
        );

        return patient;
    }


}

