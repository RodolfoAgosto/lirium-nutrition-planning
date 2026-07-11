package com.lirium.nutrition.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DailyRecordTest {

    private PatientProfile patient;
    private DailyRecord dailyRecord;

    @BeforeEach
    void setUp() {

        patient = new PatientProfile();

        dailyRecord = DailyRecord.of(
                patient,
                LocalDate.of(2026, 7, 10)
        );
    }


    @Test
    @DisplayName("Debe crear un registro diario correctamente")
    void shouldCreateDailyRecord() {

        assertNotNull(dailyRecord);
        assertEquals(
                LocalDate.of(2026, 7, 10),
                dailyRecord.getDate()
        );
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el paciente es null")
    void shouldThrowWhenPatientIsNull() {

        assertThrows(
                NullPointerException.class,
                () -> DailyRecord.of(
                        null,
                        LocalDate.now()
                )
        );
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha es null")
    void shouldThrowWhenDateIsNull() {

        assertThrows(
                NullPointerException.class,
                () -> DailyRecord.of(
                        patient,
                        null
                )
        );
    }


    @Test
    @DisplayName("Debe agregar una comida al registro")
    void shouldAddMeal() {

        MealRecord meal = new MealRecord();

        dailyRecord.addMeal(meal);

        assertEquals(1, dailyRecord.getMeals().size());
        assertTrue(dailyRecord.getMeals().contains(meal));
    }


    @Test
    @DisplayName("Debe rechazar agregar una comida null")
    void shouldThrowWhenAddingNullMeal() {

        assertThrows(
                NullPointerException.class,
                () -> dailyRecord.addMeal(null)
        );
    }


    @Test
    @DisplayName("Debe devolver lista de comidas no modificable")
    void shouldReturnUnmodifiableMeals() {

        assertThrows(
                UnsupportedOperationException.class,
                () -> dailyRecord.getMeals().add(new MealRecord())
        );
    }
}