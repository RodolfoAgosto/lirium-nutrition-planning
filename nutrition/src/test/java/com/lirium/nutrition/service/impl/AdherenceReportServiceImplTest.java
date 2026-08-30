package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdherenceReportServiceImplTest {

  @Mock private DailyRecordRepository dailyRecordRepository;

  @Mock private PatientProfileRepository patientProfileRepository;

  @Mock private NutritionPlanRepository nutritionPlanRepository;

  @InjectMocks private AdherenceReportServiceImpl service;

  private static final LocalDate START = LocalDate.of(2026, 1, 1);
  private static final LocalDate END = LocalDate.of(2026, 1, 3);

  @BeforeEach
  void setUp() {
    lenient().when(patientProfileRepository.existsById(any())).thenReturn(true);
    lenient()
        .when(nutritionPlanRepository.findFirstByPatientProfile_IdOrderByStartDateAsc(any()))
        .thenReturn(Optional.of(mockNutritionPlan()));
  }

  @Test
  void shouldCalculate100PercentAdherenceForMultipleDays() {
    // Given
    PatientProfile profile = patientProfile();

    List<DailyRecord> dailyRecords =
        List.of(
            fullDay(LocalDate.of(2026, 1, 1), profile),
            fullDay(LocalDate.of(2026, 1, 2), profile),
            fullDay(LocalDate.of(2026, 1, 3), profile));

    when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END))
        .thenReturn(dailyRecords);

    // When
    AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

    // Then
    assertAll(
        () -> assertEquals(15, result.totalExpectedMeals()),
        () -> assertEquals(15, result.totalRecordedMeals()),
        () -> assertEquals(100.0, result.adherencePercentage()),
        () -> assertEquals(3, result.daily().size()));

    verify(dailyRecordRepository)
        .findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);
  }

  @Test
  void shouldCalculatePartialAdherenceAcrossMultipleDays() {
    // Given
    PatientProfile profile = patientProfile();

    List<DailyRecord> dailyRecords =
        List.of(
            fullDay(LocalDate.of(2026, 1, 1), profile),
            partialDay(LocalDate.of(2026, 1, 2), profile),
            fullDay(LocalDate.of(2026, 1, 3), profile));

    when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END))
        .thenReturn(dailyRecords);

    // When
    AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

    // Then
    assertAll(
        () -> assertEquals(15, result.totalExpectedMeals()),
        () -> assertEquals(13, result.totalRecordedMeals()),
        () -> assertEquals(86.7, result.adherencePercentage()),
        () -> assertEquals(3, result.daily().size()));

    verify(dailyRecordRepository)
        .findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);
  }

  @Test
  void shouldHandleDaysWithoutRecords() {
    // Given
    PatientProfile profile = patientProfile();

    List<DailyRecord> dailyRecords =
        List.of(
            fullDay(LocalDate.of(2026, 1, 1), profile), fullDay(LocalDate.of(2026, 1, 3), profile));

    when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END))
        .thenReturn(dailyRecords);

    // When
    AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

    // Then
    assertAll(
        () -> assertEquals(15, result.totalExpectedMeals()),
        () -> assertEquals(10, result.totalRecordedMeals()),
        () -> assertEquals(66.7, result.adherencePercentage()),
        () -> assertEquals(3, result.daily().size()));

    verify(dailyRecordRepository)
        .findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);
  }

  @Test
  void shouldReturnZeroAdherenceWhenNoRecordsExist() {
    // Given
    PatientProfile profile = patientProfile();

    when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END))
        .thenReturn(List.of());

    // When
    AdherenceReportDTO result = service.getAdherence(profile.getId(), START, END);

    // Then
    assertAll(
        () -> assertEquals(15, result.totalExpectedMeals()),
        () -> assertEquals(0, result.totalRecordedMeals()),
        () -> assertEquals(0.0, result.adherencePercentage()),
        () -> assertEquals(3, result.daily().size()));

    verify(dailyRecordRepository)
        .findByPatient_IdAndDateBetweenWithMeals(profile.getId(), START, END);
  }

  @Test
  void shouldReturnZeroAdherenceWhenDateRangeIsEmpty() {
    // Given
    Long patientId = 1L;
    LocalDate date = LocalDate.of(2026, 1, 1);

    when(dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(patientId, date, date))
        .thenReturn(List.of());

    // When
    AdherenceReportDTO result = service.getAdherence(patientId, date, date);

    // Then
    assertAll(
        () -> assertEquals(5, result.totalExpectedMeals()),
        () -> assertEquals(0, result.totalRecordedMeals()),
        () -> assertEquals(0.0, result.adherencePercentage()),
        () -> assertEquals(1, result.daily().size()));
  }

  @Test
  void shouldThrowExceptionWhenFromIsAfterTo() {
    // Given
    Long patientId = 1L;
    LocalDate from = LocalDate.of(2026, 1, 2);
    LocalDate to = LocalDate.of(2026, 1, 1);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> service.getAdherence(patientId, from, to));
  }

  @Test
  void shouldThrowExceptionWhenPatientDoesNotExist() {
    // Given
    Long patientId = 99L;
    when(patientProfileRepository.existsById(patientId)).thenReturn(false);

    // When & Then
    assertThrows(
        ResourceNotFoundException.class, () -> service.getAdherence(patientId, START, END));
  }

  @Test
  void shouldThrowExceptionWhenFromIsBeforeEarliestStartDate() {
    // Given
    Long patientId = 1L;
    LocalDate fromPriorToPlan = LocalDate.of(2024, 12, 31); // El plan empieza el 2025-01-01

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> service.getAdherence(patientId, fromPriorToPlan, END));
  }

  private DailyRecord fullDay(LocalDate date, PatientProfile profile) {
    DailyRecord dailyRecord = DailyRecord.of(profile, date);

    for (MealType meal : MealType.values()) {
      MealRecord mealRecord =
          MealRecord.of(
              meal,
              LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 12, 0),
              dailyRecord);
      dailyRecord.addMeal(mealRecord);
    }

    return dailyRecord;
  }

  private DailyRecord partialDay(LocalDate date, PatientProfile profile) {
    DailyRecord dailyRecord = DailyRecord.of(profile, date);

    for (MealType meal : MealType.values()) {
      MealRecord mealRecord =
          MealRecord.of(
              meal,
              LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 12, 0),
              dailyRecord);
      if (meal == MealType.BREAKFAST || meal == MealType.DINNER) {
        mealRecord.markAsOverridden();
      }
      dailyRecord.addMeal(mealRecord);
    }

    return dailyRecord;
  }

  private PatientProfile patientProfile() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);

    PatientProfile profile = new PatientProfile(user);
    ReflectionTestUtils.setField(profile, "id", 1L);

    return profile;
  }

  private NutritionPlan mockNutritionPlan() {
    PatientProfile profile = patientProfile();
    NutritionPlan plan =
        NutritionPlan.generate(GoalType.WEIGHT_MAINTENANCE, 2000, 150, 200, 60, profile);

    ReflectionTestUtils.setField(plan, "id", 1L);
    plan.activate(LocalDate.of(2025, 1, 1));

    return plan;
  }
}
