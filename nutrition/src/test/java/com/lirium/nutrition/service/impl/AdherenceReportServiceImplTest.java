package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.DailyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdherenceReportServiceImplTest {

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @InjectMocks
    private AdherenceReportServiceImpl service;

    @Test
    void shouldCalculate100PercentAdherenceForMultipleDays() {

        // Given
        PatientProfile profile = patientProfile();

        List<DailyRecord> dailyRecords = List.of(
                fullDay(LocalDate.of(2026, 1, 1), profile),
                fullDay(LocalDate.of(2026, 1, 2), profile),
                fullDay(LocalDate.of(2026, 1, 3), profile)
        );

        when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(
                eq(profile.getId()),
                eq(START),
                eq(END)
        )).thenReturn(dailyRecords);

        // When
        AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

        // Then
        assertAll(
                () -> assertEquals(15, result.totalExpectedMeals()),
                () -> assertEquals(15, result.totalRecordedMeals()),
                () -> assertEquals(100.0, result.adherencePercentage()),
                () -> assertEquals(3, result.daily().size())
        );

        verify(dailyRecordRepository).findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);

    }

    @Test
    void shouldCalculatePartialAdherenceAcrossMultipleDays() {

        // Given
        PatientProfile profile = patientProfile();

        List<DailyRecord> dailyRecords = List.of(
                fullDay(LocalDate.of(2026, 1, 1), profile),
                partialDay(LocalDate.of(2026, 1, 2), profile),
                fullDay(LocalDate.of(2026, 1, 3), profile)
        );

        when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(
                eq(profile.getId()),
                eq(START),
                eq(END)
        )).thenReturn(dailyRecords);

        // When
        AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

        // Then
        assertAll(
                () -> assertEquals(15, result.totalExpectedMeals()),
                () -> assertEquals(13, result.totalRecordedMeals()),
                () -> assertEquals(86.7, result.adherencePercentage()),
                () -> assertEquals(3, result.daily().size())
        );

        verify(dailyRecordRepository).findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);

    }

    @Test
    void shouldHandleDaysWithoutRecords() {

        // Given
        PatientProfile profile = patientProfile();

        List<DailyRecord> dailyRecords = List.of(
                fullDay(LocalDate.of(2026, 1, 1), profile),
                fullDay(LocalDate.of(2026, 1, 3), profile)
        );

        when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(
                eq(profile.getId()),
                eq(START),
                eq(END)
        )).thenReturn(dailyRecords);

        // When
        AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

        // Then
        assertAll(
                () -> assertEquals(15, result.totalExpectedMeals()),
                () -> assertEquals(10, result.totalRecordedMeals()),
                () -> assertEquals(66.7, result.adherencePercentage()),
                () -> assertEquals(3, result.daily().size())
        );

        verify(dailyRecordRepository).findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);

    }

    @Test
    void shouldReturnZeroAdherenceWhenNoRecordsExist() {

        // Given
        PatientProfile profile = patientProfile();

        List<DailyRecord> dailyRecords = List.of();

        when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(
                eq(profile.getId()),
                eq(START),
                eq(END)
        )).thenReturn(dailyRecords);

        // When
        AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

        // Then
        assertAll(
                () -> assertEquals(15, result.totalExpectedMeals()),
                () -> assertEquals(0, result.totalRecordedMeals()),
                () -> assertEquals(0, result.adherencePercentage()),
                () -> assertEquals(3, result.daily().size())
        );

        verify(dailyRecordRepository).findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);

    }

    @Test
    void shouldReturnZeroAdherenceWhenDateRangeIsEmpty() {

        // Given
        Long patientId = 1L;

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        when(dailyRecordRepository
                .findByPatient_IdAndDateBetweenWithMeals(
                        patientId,
                        from,
                        to))
                .thenReturn(List.of());

        // When
        AdherenceReportDTO result =
                service.getAdherence(patientId, from, to);

        // Then
        assertEquals(0, result.totalExpectedMeals());
        assertEquals(0, result.totalRecordedMeals());
        assertEquals(0.0, result.adherencePercentage());
    }


    private DailyRecord fullDay(LocalDate date, PatientProfile profile){

        DailyRecord dailyRecord = DailyRecord.of(profile, date);

        for (MealType meal : MealType.values()) {
            MealRecord mealRecord = MealRecord.of(meal,
                                                  LocalDateTime.of(date.getYear(),date.getMonth(),date.getDayOfMonth(),12, 0),
                                                 dailyRecord);
            dailyRecord.addMeal(mealRecord);
        }

        return dailyRecord;

    }

    private DailyRecord partialDay(LocalDate date, PatientProfile profile){

        DailyRecord dailyRecord = DailyRecord.of(profile, date);

        for (MealType meal : MealType.values()) {
            MealRecord mealRecord = MealRecord.of(meal,
                    LocalDateTime.of(date.getYear(),date.getMonth(),date.getDayOfMonth(),12, 0),dailyRecord);
            if(meal == MealType.BREAKFAST || meal == MealType.DINNER)
                mealRecord.markAsOverridden();
            dailyRecord.addMeal(mealRecord);
        }

        return dailyRecord;

    }

    private PatientProfile patientProfile() {
        return new User(
                "john.deer@gmail.com",
                "hash",
                "John",
                "Deer",
                Role.PATIENT
        ).getPatientProfile();
    }

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    private static final LocalDate END = LocalDate.of(2026, 1, 3);

}