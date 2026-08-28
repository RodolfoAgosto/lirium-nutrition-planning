package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.DailyNutritionComparisonDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.MealRecordRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.NutritionPlanService;
import com.lirium.nutrition.service.PatientProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyRecordServiceImplTest {

    @Mock
    DailyRecordRepository dailyRecordRepository;
    @Mock
    PatientProfileService patientProfileService;
    @Mock
    NutritionPlanService nutritionPlanService;
    @Mock
    FoodService foodService;
    @Mock
    private PatientProfileRepository patientProfileRepository;
    @Mock
    MealRecordRepository mealRecordRepository;
    @Mock
    private Clock clock;

    @InjectMocks
    DailyRecordServiceImpl service;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    private static final LocalDate END = LocalDate.of(2026, 1, 3);

    @Test
    void shouldThrowWhenCreatingDailyRecordForFutureDate() {

        Long patientId = 1L;
        LocalDate futureDate = mockClock().plusDays(1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOrCreateForDate(patientId, futureDate)
        );

        assertEquals(
                "Cannot create daily records for future dates",
                ex.getMessage()
        );

        verifyNoInteractions(dailyRecordRepository);
        verifyNoInteractions(patientProfileService);
        verifyNoInteractions(nutritionPlanService);
    }


    @Test
    void shouldThrowWhenDateIsBeforeActivePlanStartDate() {

        Long patientId = 1L;

        LocalDate planStart = LocalDate.of(2026, 1, 10);
        LocalDate targetDate = LocalDate.of(2026, 1, 5);

        PatientProfile patient = patientProfile();
        NutritionPlan plan = mock(NutritionPlan.class);

        when(dailyRecordRepository.findByPatient_IdAndDate(
                patientId,
                targetDate
        )).thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(plan.getStartDate())
                .thenReturn(planStart);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOrCreateForDate(patientId, targetDate)
        );

        assertTrue(ex.getMessage().contains(targetDate.toString()));
        assertTrue(ex.getMessage().contains(planStart.toString()));

        verify(dailyRecordRepository, never())
                .save(any(DailyRecord.class));
    }


    @Test
    void shouldThrowWhenDateRangeIsInvalid() {

        Long patientId = 1L;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getNutritionComparison(
                        patientId,
                        END,
                        START
                )
        );

        assertEquals(
                "The 'from' date cannot be after the 'to' date",
                ex.getMessage()
        );

        verifyNoInteractions(patientProfileRepository);
        verifyNoInteractions(nutritionPlanService);
        verifyNoInteractions(dailyRecordRepository);
    }


    @Test
    void shouldThrowWhenFromDateIsNull() {

        Long patientId = 1L;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getNutritionComparison(
                        patientId,
                        null,
                        END
                )
        );

        assertEquals(
                "Date range is required",
                ex.getMessage()
        );

        verifyNoInteractions(patientProfileRepository);
        verifyNoInteractions(nutritionPlanService);
        verifyNoInteractions(dailyRecordRepository);
    }


    @Test
    void shouldThrowWhenToDateIsNull() {

        Long patientId = 1L;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getNutritionComparison(
                        patientId,
                        START,
                        null
                )
        );

        assertEquals(
                "Date range is required",
                ex.getMessage()
        );

        verifyNoInteractions(patientProfileRepository);
        verifyNoInteractions(nutritionPlanService);
        verifyNoInteractions(dailyRecordRepository);
    }

    @Test
    void shouldAdjustEffectiveFromToPlanStartDate() {

        Long patientId = 1L;

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate planStart = LocalDate.of(2026, 1, 5);
        LocalDate to = LocalDate.of(2026, 1, 10);

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                null
        );

        plan.activate(planStart);

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                patientId,
                planStart,
                to
        )).thenReturn(List.of());

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        from,
                        to
                );

        assertEquals(planStart, result.from());
        assertEquals(to, result.to());
        assertEquals(6, result.days().size());

        verify(patientProfileRepository)
                .existsById(patientId);

        verify(dailyRecordRepository)
                .findByPatient_IdAndDateBetween(
                        patientId,
                        planStart,
                        to
                );
    }

    @Test
    void shouldReturnEmptyReportWhenRequestedRangeIsBeforePlanStart() {

        Long patientId = 1L;

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 3);
        LocalDate planStart = LocalDate.of(2026, 1, 10);

        NutritionPlan plan = mock(NutritionPlan.class);

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(plan.getStartDate())
                .thenReturn(planStart);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        from,
                        to
                );

        assertAll(
                () -> assertEquals(from, result.from()),
                () -> assertEquals(to, result.to()),
                () -> assertTrue(result.days().isEmpty())
        );

        verify(dailyRecordRepository, never())
                .findByPatient_IdAndDateBetween(
                        anyLong(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldRoundAdherenceToOneDecimalPlace() {

        Long patientId = 1L;

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = from;

        NutritionPlan plan = createPlan(
                2000,
                150,
                200,
                70
        );

        DailyRecord record = mock(DailyRecord.class);
        MealRecord meal = mock(MealRecord.class);
        FoodPortionRecord portion = mock(FoodPortionRecord.class);

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(record.getDate()).thenReturn(from);
        when(record.getMeals()).thenReturn(List.of(meal));

        when(meal.getFoodPortions()).thenReturn(List.of(portion));

        when(portion.calories())
                .thenReturn(new Calories(1234));

        when(portion.protein())
                .thenReturn(new Protein(50));

        when(portion.carbs())
                .thenReturn(new Carbs(100));

        when(portion.fat())
                .thenReturn(new Fat(20));

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                patientId,
                from,
                to
        )).thenReturn(List.of(record));

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        from,
                        to
                );

        assertEquals(
                61.7,
                result.days().getFirst().adherencePercentage()
        );
    }

    @Test
    void shouldReturnTrueWhenDailyRecordBelongsToUser() {

        Long dailyRecordId = 1L;
        Long userId = 10L;

        User user = mock(User.class);
        PatientProfile patient = mock(PatientProfile.class);
        DailyRecord record = mock(DailyRecord.class);

        when(user.getId()).thenReturn(userId);
        when(patient.getUser()).thenReturn(user);
        when(record.getPatient()).thenReturn(patient);

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(record));

        assertTrue(
                service.isDailyRecordOwnedByUser(
                        dailyRecordId,
                        userId
                )
        );
    }


    @Test
    void shouldReturnFalseWhenDailyRecordDoesNotBelongToUser() {

        Long dailyRecordId = 1L;
        Long ownerUserId = 10L;
        Long authenticatedUserId = 20L;

        User user = mock(User.class);
        PatientProfile patient = mock(PatientProfile.class);
        DailyRecord record = mock(DailyRecord.class);

        when(user.getId()).thenReturn(ownerUserId);
        when(patient.getUser()).thenReturn(user);
        when(record.getPatient()).thenReturn(patient);

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(record));

        assertFalse(
                service.isDailyRecordOwnedByUser(
                        dailyRecordId,
                        authenticatedUserId
                )
        );
    }


    @Test
    void shouldReturnFalseWhenDailyRecordDoesNotExistForOwnershipCheck() {

        Long dailyRecordId = 99L;
        Long userId = 10L;

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.empty());

        assertFalse(
                service.isDailyRecordOwnedByUser(
                        dailyRecordId,
                        userId
                )
        );
    }


    @Test
    void shouldReturnTrueWhenMealRecordBelongsToUser() {

        Long mealRecordId = 1L;
        Long userId = 10L;

        User user = mock(User.class);
        PatientProfile patient = mock(PatientProfile.class);
        DailyRecord dailyRecord = mock(DailyRecord.class);
        MealRecord mealRecord = mock(MealRecord.class);

        when(user.getId()).thenReturn(userId);
        when(patient.getUser()).thenReturn(user);
        when(dailyRecord.getPatient()).thenReturn(patient);
        when(mealRecord.getDailyRecord()).thenReturn(dailyRecord);

        when(mealRecordRepository.findById(mealRecordId))
                .thenReturn(Optional.of(mealRecord));

        assertTrue(
                service.isMealRecordOwnedByUser(
                        mealRecordId,
                        userId
                )
        );
    }


    @Test
    void shouldReturnFalseWhenMealRecordDoesNotBelongToUser() {

        Long mealRecordId = 1L;
        Long ownerUserId = 10L;
        Long authenticatedUserId = 20L;

        User user = mock(User.class);
        PatientProfile patient = mock(PatientProfile.class);
        DailyRecord dailyRecord = mock(DailyRecord.class);
        MealRecord mealRecord = mock(MealRecord.class);

        when(user.getId()).thenReturn(ownerUserId);
        when(patient.getUser()).thenReturn(user);
        when(dailyRecord.getPatient()).thenReturn(patient);
        when(mealRecord.getDailyRecord()).thenReturn(dailyRecord);

        when(mealRecordRepository.findById(mealRecordId))
                .thenReturn(Optional.of(mealRecord));

        assertFalse(
                service.isMealRecordOwnedByUser(
                        mealRecordId,
                        authenticatedUserId
                )
        );
    }


    @Test
    void shouldReturnFalseWhenMealRecordDoesNotExistForOwnershipCheck() {

        Long mealRecordId = 99L;
        Long userId = 10L;

        when(mealRecordRepository.findById(mealRecordId))
                .thenReturn(Optional.empty());

        assertFalse(
                service.isMealRecordOwnedByUser(
                        mealRecordId,
                        userId
                )
        );
    }


    @Test
    void shouldReturnTrueWhenDailyRecordExistsForPatientAndDate() {

        Long patientId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);

        when(dailyRecordRepository.existsByPatient_IdAndDate(
                patientId,
                date
        )).thenReturn(true);

        assertTrue(
                service.existsForPatientAndDate(
                        patientId,
                        date
                )
        );

        verify(dailyRecordRepository)
                .existsByPatient_IdAndDate(
                        patientId,
                        date
                );
    }


    @Test
    void shouldReturnFalseWhenDailyRecordDoesNotExistForPatientAndDate() {

        Long patientId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);

        when(dailyRecordRepository.existsByPatient_IdAndDate(
                patientId,
                date
        )).thenReturn(false);

        assertFalse(
                service.existsForPatientAndDate(
                        patientId,
                        date
                )
        );

        verify(dailyRecordRepository)
                .existsByPatient_IdAndDate(
                        patientId,
                        date
                );
    }

    @Test
    void shouldReturnDailyRecordById() {

        // Given
        Long id = 1L;

        PatientProfile patient = patientProfile();

        DailyRecord record = DailyRecord.of(patient, LocalDate.of(2026, 1, 1));

        when(dailyRecordRepository.findById(id))
                .thenReturn(Optional.of(record));
        // When
        DailyRecordResponseDTO result = service.getById(id);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(record.getDate(), result.date()),
                () -> assertEquals(patient.getId(), result.id())
        );

        verify(dailyRecordRepository).findById(id);

    }

    @Test
    void shouldThrowExceptionWhenDailyRecordNotFound() {

        // Given
        Long id = 99L;

        when(dailyRecordRepository.findById(id))
                .thenReturn(Optional.empty());

        // When + Then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.getById(id)
        );

        assertTrue(ex.getMessage().contains("DailyRecord"));
        assertTrue(ex.getMessage().contains(id.toString()));

        verify(dailyRecordRepository).findById(id);

    }

    @Test
    void shouldReturnDailyRecordsForPatient() {

        // Given
        Long patientId = 1L;

        PatientProfile patient = patientProfile();

        DailyRecord record =
                DailyRecord.of(patient, LocalDate.of(2026, 1, 1));

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(dailyRecordRepository.findByPatient_IdOrderByDateDesc(patientId))
                .thenReturn(List.of(record));

        // When
        List<DailyRecordResponseDTO> result =
                service.getByPatient(patientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(patientProfileRepository)
                .existsById(patientId);

        verify(dailyRecordRepository)
                .findByPatient_IdOrderByDateDesc(patientId);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecords() {

        Long patientId = 1L;

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(dailyRecordRepository.findByPatient_IdOrderByDateDesc(patientId))
                .thenReturn(List.of());

        List<DailyRecordResponseDTO> result =
                service.getByPatient(patientId);

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(patientProfileRepository)
                .existsById(patientId);

        verify(dailyRecordRepository)
                .findByPatient_IdOrderByDateDesc(patientId);
    }

    @Test
    void shouldReturnExistingDailyRecord() {
        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();
        DailyRecord record = DailyRecord.of(patient, today);

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.of(record));

        // When (pasando null o today a getOrCreateForDate)
        DailyRecordResponseDTO result = service.getOrCreateForDate(patientId, null);

        // Then
        assertNotNull(result);
        assertEquals(today, result.date());

        verify(dailyRecordRepository).findByPatient_IdAndDate(patientId, today);
        verifyNoInteractions(patientProfileService);
        verifyNoInteractions(nutritionPlanService);
    }

    @Test
    void shouldCreateDailyRecordWhenNotExists() {
        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();

        NutritionPlan plan = mock(NutritionPlan.class);
        when(plan.getStartDate()).thenReturn(today.minusDays(10));
        when(plan.getWeek()).thenReturn(Collections.emptyList()); // O con DailyPlans si querés testear comidas

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DailyRecordResponseDTO result = service.getOrCreateForDate(patientId, today);

        // Then
        assertNotNull(result);
        assertEquals(today, result.date());

        ArgumentCaptor<DailyRecord> captor = ArgumentCaptor.forClass(DailyRecord.class);
        verify(dailyRecordRepository).save(captor.capture());

        DailyRecord saved = captor.getValue();
        assertEquals(today, saved.getDate());
        assertEquals(patient, saved.getPatient());

        verify(patientProfileService).findById(patientId);
        verify(nutritionPlanService).findActivePlan(patientId);
    }

    @Test
    void shouldThrowExceptionWhenNoActivePlan() {

        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.empty());

        // When + Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.getOrCreateForDate(patientId, today)
        );

        assertEquals("Patient has no active nutrition plan. Cannot create daily record.", exception.getMessage());

        verify(patientProfileService).findById(patientId);
        verify(nutritionPlanService).findActivePlan(patientId);
        verify(dailyRecordRepository, never()).save(any());
    }

    @Test
    void shouldOverrideMealWhenNotesProvided() {

        // Given
        Long mealId = 1L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, mockClock());

        MealRecord meal = mock(MealRecord.class);

        when(meal.getId()).thenReturn(mealId);

        dailyRecord.addMeal(meal);

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("Patient changed meal");

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.of(dailyRecord));

        // When
        service.updateMeal(mealId, request);

        // Then
        verify(meal).markAsOverridden("Patient changed meal");
        verify(dailyRecordRepository).save(dailyRecord);
    }

    @Test
    void shouldAddFoodPortionToMeal() {

        // Given
        Long mealId = 1L;
        Long foodId = 10L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, mockClock());

        MealRecord meal = mock(MealRecord.class);
        when(meal.getId()).thenReturn(mealId);

        dailyRecord.addMeal(meal);

        Food food = mock(Food.class);

        FoodPortionAddRequestDTO request =
                new FoodPortionAddRequestDTO(
                        foodId,
                        100D,
                        MeasureUnit.GRAM
                );

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.of(dailyRecord));

        when(foodService.findEntityById(foodId))
                .thenReturn(food);

        // When
        service.addPortion(mealId, request);

        // Then
        verify(foodService).findEntityById(foodId);

        verify(meal).addFoodPortion(
                food,
                100D,
                MeasureUnit.GRAM
        );

        verify(dailyRecordRepository).save(dailyRecord);
    }

    @Test
    void shouldThrowWhenMealNotFoundInAddPortion() {

        // Given
        Long mealId = 1L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, mockClock());

        FoodPortionAddRequestDTO request =
                new FoodPortionAddRequestDTO(
                        10L,
                        100D,
                        MeasureUnit.GRAM
                );

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.of(dailyRecord));

        // When + Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.addPortion(mealId, request)
        );

        verify(foodService, never()).findEntityById(anyLong());
        verify(dailyRecordRepository, never()).save(any());
    }

    @Test
    void shouldRemovePortionFromMeal() {

        // Given
        Long dailyRecordId = 1L;
        Long mealId = 10L;
        Long portionId = 100L;

        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, today);

        NutritionPlan activePlan = mock(NutritionPlan.class);
        when(nutritionPlanService.findActivePlan(patient.getId()))
                .thenReturn(Optional.of(activePlan));
        when(activePlan.getStartDate())
                .thenReturn(today.minusDays(5));

        MealRecord meal = mock(MealRecord.class);
        FoodPortionRecord portion = mock(FoodPortionRecord.class);

        when(meal.getId()).thenReturn(mealId);
        when(meal.getFoodPortions()).thenReturn(List.of(portion));
        when(portion.getId()).thenReturn(portionId);

        dailyRecord.addMeal(meal);

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When
        service.removePortion(dailyRecordId, mealId, portionId);

        // Then
        verify(meal).markAsOverridden();
        verify(meal).removeFoodPortion(portion);
        verify(dailyRecordRepository).save(dailyRecord);
    }

    @Test
    void shouldThrowWhenMealNotFoundInRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealId = 10L;
        Long portionId = 100L;

        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = mock(DailyRecord.class);
        when(dailyRecord.getPatient()).thenReturn(patient);
        when(dailyRecord.getDate()).thenReturn(today);

        NutritionPlan activePlan = mock(NutritionPlan.class);
        when(nutritionPlanService.findActivePlan(patient.getId()))
                .thenReturn(Optional.of(activePlan));
        when(activePlan.getStartDate())
                .thenReturn(today.minusDays(5));

        when(dailyRecord.getMeals())
                .thenReturn(Collections.emptyList());

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When + Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealId,
                        portionId
                )
        );

        verify(dailyRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPortionNotFoundInRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealId = 10L;
        Long portionId = 100L;

        LocalDate today = mockClock();

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, today);

        NutritionPlan activePlan = mock(NutritionPlan.class);
        when(nutritionPlanService.findActivePlan(patient.getId()))
                .thenReturn(Optional.of(activePlan));
        when(activePlan.getStartDate())
                .thenReturn(today.minusDays(5));

        MealRecord meal = mock(MealRecord.class);
        when(meal.getId()).thenReturn(mealId);
        when(meal.getFoodPortions()).thenReturn(List.of());

        dailyRecord.addMeal(meal);

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When + Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealId,
                        portionId
                )
        );

        verify(dailyRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNoActivePlanExists() {

        Long patientId = 1L;

        // Mock para validar que el paciente sí existe en la base de datos
        when(patientProfileRepository.existsById(patientId)).thenReturn(true);

        // Mock para simular que no tiene plan activo
        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.empty());

        // Asertar la excepción correcta
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getNutritionComparison(
                        patientId,
                        START,
                        END
                )
        );

        verify(dailyRecordRepository, never())
                .findByPatient_IdAndDateBetween(anyLong(), any(), any());
    }

    @Test
    void shouldReturnZeroConsumedValuesWhenNoRecordsExist() {

        Long patientId = 1L;

        NutritionPlan plan = createPlan(2000, 150, 200, 70);

        // Inyecta las fechas directamente en los atributos privados del plan
        org.springframework.test.util.ReflectionTestUtils.setField(
                plan, "startDate", START
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                plan, "endDate", END
        );

        when(patientProfileRepository.existsById(patientId))
                .thenReturn(true);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                patientId,
                START,
                END
        )).thenReturn(List.of());

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        START,
                        END
                );

        DailyNutritionComparisonDTO day = result.days().get(0);

        assertAll(
                () -> assertEquals(2000, day.targetCalories()),
                () -> assertEquals(0, day.consumedCalories()),
                () -> assertEquals(0.0, day.adherencePercentage())
        );
    }

    @Test
    void shouldSumConsumedNutrientsFromFoodPortions() {

        NutritionPlan plan = createPlan(
                2000,
                150,
                200,
                70
        );

        DailyRecord record = createRecordWithPortions();

        when(patientProfileRepository.existsById(1L))
                .thenReturn(true);

        when(nutritionPlanService.findActivePlan(1L))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                1L,
                START,
                END
        )).thenReturn(List.of(record));

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        1L,
                        START,
                        END
                );

        DailyNutritionComparisonDTO day =
                result.days().get(0);

        assertAll(
                () -> assertEquals(300, day.consumedCalories()),
                () -> assertEquals(30, day.consumedProtein()),
                () -> assertEquals(50, day.consumedCarbs()),
                () -> assertEquals(15, day.consumedFat())
        );
    }

    @Test
    void shouldReturnZeroAdherenceWhenTargetCaloriesAreZero() {

        NutritionPlan plan =
                createPlan(
                        0,
                        0,
                        0,
                        0
                );

        when(patientProfileRepository.existsById(1L))
                .thenReturn(true);

        when(nutritionPlanService.findActivePlan(1L))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                1L,
                START,
                END
        )).thenReturn(List.of());

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        1L,
                        START,
                        END
                );

        assertEquals(
                0.0,
                result.days().get(0).adherencePercentage()
        );
    }


    @Test
    void shouldCapAdherenceAt100Percent() {

        NutritionPlan plan =
                createPlan(
                        2000,
                        150,
                        200,
                        70
                );

        DailyRecord record = createRecordWith5000Calories();

        when(patientProfileRepository.existsById(1L))
                .thenReturn(true);

        when(nutritionPlanService.findActivePlan(1L))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                1L,
                START,
                END
        )).thenReturn(List.of(record));

        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        1L,
                        START,
                        END
                );

        assertEquals(
                100.0,
                result.days().get(0).adherencePercentage()
        );
    }

    @Test
    void shouldThrowWhenDailyRecordNotFoundInUpdateMeal() {

        Long mealId = 1L;

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.empty());

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("notes");

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateMeal(mealId, request)
        );

        verify(dailyRecordRepository)
                .findByMealRecordId(mealId);

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenMealNotFoundInUpdateMeal() {

        Long mealId = 1L;

        PatientProfile patient = patientProfile();

        DailyRecord dailyRecord =
                DailyRecord.of(patient, mockClock());

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.of(dailyRecord));

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("notes");

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateMeal(mealId, request)
        );

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenDailyRecordNotFoundInAddPortion() {

        Long mealId = 1L;

        FoodPortionAddRequestDTO request =
                new FoodPortionAddRequestDTO(
                        10L,
                        100D,
                        MeasureUnit.GRAM
                );

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.addPortion(mealId, request)
        );

        verify(foodService, never())
                .findEntityById(anyLong());

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateTodayRecordWithMealsFromActivePlan() {

        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        PatientProfile patient = mock(PatientProfile.class);
        NutritionPlan activePlan = mock(NutritionPlan.class);
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PlanMeal planMeal = mock(PlanMeal.class);

        when(activePlan.getStartDate()).thenReturn(today.minusDays(1));

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(activePlan));

        when(activePlan.getWeek())
                .thenReturn(List.of(dailyPlan));

        when(dailyPlan.getDayOfWeek())
                .thenReturn(today.getDayOfWeek());

        when(dailyPlan.getMeals())
                .thenReturn(List.of(planMeal));

        when(planMeal.getType())
                .thenReturn(MealType.BREAKFAST);

        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DailyRecordResponseDTO result = service.getOrCreateForDate(patientId, today);

        // Then
        assertNotNull(result);

        ArgumentCaptor<DailyRecord> captor = ArgumentCaptor.forClass(DailyRecord.class);
        verify(dailyRecordRepository).save(captor.capture());

        DailyRecord savedRecord = captor.getValue();
        assertEquals(1, savedRecord.getMeals().size());
        assertEquals(MealType.BREAKFAST, savedRecord.getMeals().get(0).getType());

        verify(dailyPlan).getMeals();
    }

    @Test
    void shouldCreateRecordWithoutMealsWhenTodayDailyPlanDoesNotExist() {

        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        User user = generateUser();
        PatientProfile patient = user.getPatientProfile();

        NutritionPlan plan = mock(NutritionPlan.class);

        when(plan.getStartDate()).thenReturn(today.minusDays(1));

        DailyPlan otherDay = mock(DailyPlan.class);

        when(otherDay.getDayOfWeek())
                .thenReturn(today.getDayOfWeek().plus(1));

        when(plan.getWeek())
                .thenReturn(List.of(otherDay));

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyRecordResponseDTO result = service.getOrCreateForDate(patientId, today);

        // Then
        assertNotNull(result);

        ArgumentCaptor<DailyRecord> captor = ArgumentCaptor.forClass(DailyRecord.class);
        verify(dailyRecordRepository).save(captor.capture());

        DailyRecord saved = captor.getValue();
        assertTrue(saved.getMeals().isEmpty(), "El registro debería crearse con la lista de comidas vacía");

        verify(otherDay, never()).getMeals();
    }

    @ParameterizedTest
    @EnumSource(MealType.class)
    void shouldCreateTodayRecordWithMealsFromActivePlan(MealType mealType) {

        // Given
        Long patientId = 1L;
        LocalDate today = mockClock();

        PatientProfile patient = mock(PatientProfile.class);
        NutritionPlan activePlan = mock(NutritionPlan.class);
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PlanMeal planMeal = mock(PlanMeal.class);

        when(activePlan.getStartDate()).thenReturn(today.minusDays(1));

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(activePlan));

        when(activePlan.getWeek())
                .thenReturn(List.of(dailyPlan));

        when(dailyPlan.getDayOfWeek())
                .thenReturn(today.getDayOfWeek());

        when(dailyPlan.getMeals())
                .thenReturn(List.of(planMeal));

        when(planMeal.getType())
                .thenReturn(mealType);

        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyRecordResponseDTO result = service.getOrCreateForDate(patientId, today);

        // Then
        assertNotNull(result);

        ArgumentCaptor<DailyRecord> captor = ArgumentCaptor.forClass(DailyRecord.class);
        verify(dailyRecordRepository).save(captor.capture());

        DailyRecord savedRecord = captor.getValue();
        assertEquals(1, savedRecord.getMeals().size());

        assertEquals(mealType, savedRecord.getMeals().get(0).getType());
    }

    @Test
    void shouldNotOverrideMealWhenNotesAreNull() {

        // Given
        Long mealRecordId = 1L;

        DailyRecord dailyRecord = mock(DailyRecord.class);
        MealRecord mealRecord = mock(MealRecord.class);

        when(mealRecord.getId()).thenReturn(mealRecordId);

        when(dailyRecord.getMeals())
                .thenReturn(List.of(mealRecord));

        when(dailyRecordRepository.findByMealRecordId(mealRecordId))
                .thenReturn(Optional.of(dailyRecord));

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO(null);

        // When
        service.updateMeal(mealRecordId, request);

        // Then
        verify(mealRecord, never())
                .markAsOverridden(anyString());

        verify(dailyRecordRepository)
                .save(dailyRecord);
    }

    @Test
    void shouldThrowWhenMealRecordNotFoundForUpdate() {

        // Given
        Long mealRecordId = 1L;

        DailyRecord dailyRecord = mock(DailyRecord.class);

        MealRecord anotherMeal = mock(MealRecord.class);
        when(anotherMeal.getId()).thenReturn(999L);

        when(dailyRecord.getMeals())
                .thenReturn(List.of(anotherMeal));

        when(dailyRecordRepository.findByMealRecordId(mealRecordId))
                .thenReturn(Optional.of(dailyRecord));

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO("notes");

        // When + Then
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateMeal(mealRecordId, request)
        );

        assertTrue(ex.getMessage().contains("MealRecord"));

        verify(dailyRecordRepository)
                .findByMealRecordId(mealRecordId);

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenDailyRecordNotFoundForRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealRecordId = 2L;
        Long portionId = 3L;

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.empty());

        // When + Then
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealRecordId,
                        portionId
                )
        );

        assertTrue(ex.getMessage().contains("DailyRecord"));

        verify(dailyRecordRepository)
                .findById(dailyRecordId);

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenMealRecordNotFoundForRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealRecordId = 2L;
        Long portionId = 3L;

        LocalDate today = mockClock();

        PatientProfile patient = mock(PatientProfile.class);
        when(patient.getId()).thenReturn(50L);

        DailyRecord dailyRecord = mock(DailyRecord.class);
        when(dailyRecord.getPatient()).thenReturn(patient);
        when(dailyRecord.getDate()).thenReturn(today);

        NutritionPlan activePlan = mock(NutritionPlan.class);
        when(nutritionPlanService.findActivePlan(patient.getId()))
                .thenReturn(Optional.of(activePlan));
        when(activePlan.getStartDate())
                .thenReturn(today.minusDays(5));

        MealRecord anotherMeal = mock(MealRecord.class);
        when(anotherMeal.getId()).thenReturn(999L);

        when(dailyRecord.getMeals())
                .thenReturn(List.of(anotherMeal));

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When + Then
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealRecordId,
                        portionId
                )
        );

        assertTrue(ex.getMessage().contains("MealRecord"));

        verify(dailyRecordRepository)
                .findById(dailyRecordId);

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenFoodPortionNotFoundForRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealRecordId = 2L;
        Long portionId = 3L;

        LocalDate today = mockClock();

        PatientProfile patient = mock(PatientProfile.class);
        when(patient.getId()).thenReturn(50L);

        DailyRecord dailyRecord = mock(DailyRecord.class);
        when(dailyRecord.getPatient()).thenReturn(patient);
        when(dailyRecord.getDate()).thenReturn(today);

        NutritionPlan activePlan = mock(NutritionPlan.class);
        when(nutritionPlanService.findActivePlan(patient.getId()))
                .thenReturn(Optional.of(activePlan));
        when(activePlan.getStartDate())
                .thenReturn(today.minusDays(5));

        MealRecord mealRecord = mock(MealRecord.class);
        FoodPortionRecord anotherPortion = mock(FoodPortionRecord.class);

        when(anotherPortion.getId()).thenReturn(999L);

        when(mealRecord.getId()).thenReturn(mealRecordId);
        when(mealRecord.getFoodPortions())
                .thenReturn(List.of(anotherPortion));

        when(dailyRecord.getMeals())
                .thenReturn(List.of(mealRecord));

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When + Then
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealRecordId,
                        portionId
                )
        );

        assertTrue(ex.getMessage().contains("FoodPortionRecord"));

        verify(dailyRecordRepository)
                .findById(dailyRecordId);

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenPatientDoesNotExist() {

        when(patientProfileRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getNutritionComparison(
                        1L,
                        START,
                        END
                )
        );

        verify(patientProfileRepository).existsById(1L);
    }

    private DailyRecord createRecordWith5000Calories() {

        FoodPortionRecord portion = mock(FoodPortionRecord.class);

        when(portion.calories()).thenReturn(new Calories(5000));
        when(portion.protein()).thenReturn(new Protein(100));
        when(portion.carbs()).thenReturn(new Carbs(100));
        when(portion.fat()).thenReturn(new Fat(100));

        MealRecord meal = mock(MealRecord.class);

        when(meal.getFoodPortions())
                .thenReturn(List.of(portion));

        DailyRecord record = mock(DailyRecord.class);

        when(record.getDate()).thenReturn(START);
        when(record.getMeals()).thenReturn(List.of(meal));

        return record;
    }

    private DailyRecord createRecordWithPortions() {

        FoodPortionRecord portion = mock(FoodPortionRecord.class);

        when(portion.calories()).thenReturn(new Calories(300));
        when(portion.protein()).thenReturn(new Protein(30));
        when(portion.carbs()).thenReturn(new Carbs(50));
        when(portion.fat()).thenReturn(new Fat(15));

        MealRecord meal = mock(MealRecord.class);

        when(meal.getFoodPortions())
                .thenReturn(List.of(portion));

        DailyRecord record = mock(DailyRecord.class);

        when(record.getDate()).thenReturn(START);
        when(record.getMeals()).thenReturn(List.of(meal));

        return record;
    }

    private PatientProfile patientProfile() {

        Sex sex = Sex.MALE;
        ActivityLevel activityLevel = ActivityLevel.ACTIVE;
        GoalType goal= GoalType.MUSCLE_GAIN;
        List<PhysiologicalCondition> conditions = List.of();

        User user = new User(
                "test@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setBirthDate(mockClock().minusYears(30));

        PatientProfile patient = user.getPatientProfile();

        patient.update(
                sex,
                activityLevel,
                Weight.of(55_000),
                Height.of(165),
                null,
                Set.of(),
                conditions,
                goal
        );

        return patient;
    }

    private NutritionPlan createPlan(
            int calories,
            int protein,
            int carbs,
            int fat) {

        NutritionPlan plan = mock(NutritionPlan.class);

        when(plan.getDailyCalories()).thenReturn(calories);
        when(plan.getProteinGrams()).thenReturn(protein);
        when(plan.getCarbGrams()).thenReturn(carbs);
        when(plan.getFatGrams()).thenReturn(fat);

        return plan;
    }

    private User generateUser(){

        User user = new User(
                "john@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setDni("12345678");

        PatientProfile profile = user.getPatientProfile();

        profile.update(
                Sex.MALE,
                ActivityLevel.MODERATE,
                Weight.of(80000),
                Height.of(180),
                "notes",
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        Restriction restriction = mock(Restriction.class);

        profile.addRestriction(restriction);

        return user;
    }

    private LocalDate mockClock() {
        when(clock.instant())
                .thenReturn(Instant.parse("2026-08-28T15:00:00Z"));

        when(clock.getZone())
                .thenReturn(ZoneId.of("America/Argentina/Buenos_Aires"));

        return LocalDate.now(clock);
    }


}