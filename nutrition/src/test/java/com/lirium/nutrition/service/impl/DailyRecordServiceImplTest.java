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

import java.time.LocalDate;
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

    @InjectMocks
    DailyRecordServiceImpl service;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    private static final LocalDate END = LocalDate.of(2026, 1, 3);

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

        DailyRecord r1 = DailyRecord.of(patient, LocalDate.of(2026, 1, 1));
        DailyRecord r2 = DailyRecord.of(patient, LocalDate.of(2026, 1, 2));

        when(dailyRecordRepository.findByPatient_IdOrderByDateDesc(patientId))
                .thenReturn(List.of(r1, r2));

        // When
        List<DailyRecordResponseDTO> result = service.getByPatient(patientId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size())
        );

        verify(dailyRecordRepository).findByPatient_IdOrderByDateDesc(patientId);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecords() {

        // Given
        Long patientId = 1L;

        when(dailyRecordRepository.findByPatient_IdOrderByDateDesc(patientId))
                .thenReturn(List.of());

        // When
        List<DailyRecordResponseDTO> result = service.getByPatient(patientId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(dailyRecordRepository).findByPatient_IdOrderByDateDesc(patientId);
    }

    @Test
    void shouldReturnExistingDailyRecord() {

        // Given
        Long patientId = 1L;
        LocalDate today = LocalDate.now();

        PatientProfile patient = patientProfile();

        DailyRecord record = DailyRecord.of(patient, today);

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.of(record));

        // When
        DailyRecordResponseDTO result = service.getOrCreateToday(patientId);

        // Then
        assertNotNull(result);

        verify(dailyRecordRepository).findByPatient_IdAndDate(patientId, today);

        verifyNoInteractions(patientProfileService);
        verifyNoInteractions(nutritionPlanService);
    }

    @Test
    void shouldCreateDailyRecordWhenNotExists() {

        // Given
        Long patientId = 1L;
        LocalDate today = LocalDate.now();

        PatientProfile patient = patientProfile();

        NutritionPlan plan = mock(NutritionPlan.class);

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        // When
        DailyRecordResponseDTO result = service.getOrCreateToday(patientId);

        // Then
        assertNotNull(result);

        ArgumentCaptor<DailyRecord> captor = ArgumentCaptor.forClass(DailyRecord.class);

        verify(dailyRecordRepository).save(captor.capture());

        DailyRecord saved = captor.getValue();

        assertEquals(today, saved.getDate());
        assertEquals(patient, saved.getPatient());

        verify(patientProfileService).findById(patientId);
        verify(nutritionPlanService).findActivePlan(patientId);
        verify(dailyRecordRepository).save(any(DailyRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenNoActivePlan() {

        // Given
        Long patientId = 1L;
        LocalDate today = LocalDate.now();

        PatientProfile patient = patientProfile();

        when(dailyRecordRepository.findByPatient_IdAndDate(patientId, today))
                .thenReturn(Optional.empty());

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.empty());

        // When + Then
        assertThrows(
                IllegalStateException.class,
                () -> service.getOrCreateToday(patientId)
        );

        verify(patientProfileService).findById(1L);
        verify(nutritionPlanService).findActivePlan(patientId);
        verify(dailyRecordRepository, never()).save(any());
    }

    @Test
    void shouldOverrideMealWhenNotesProvided() {

        // Given
        Long mealId = 1L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

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
    void shouldUpdateMealWithoutOverrideWhenNotesAreNull() {

        // Given
        Long mealId = 1L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

        MealRecord meal = mock(MealRecord.class);

        when(meal.getId()).thenReturn(mealId);

        dailyRecord.addMeal(meal);

        MealRecordUpdateRequestDTO request =
                new MealRecordUpdateRequestDTO(null);

        when(dailyRecordRepository.findByMealRecordId(mealId))
                .thenReturn(Optional.of(dailyRecord));

        // When
        service.updateMeal(mealId, request);

        // Then
        verify(meal, never()).markAsOverridden(anyString());
        verify(dailyRecordRepository).save(dailyRecord);
    }

    @Test
    void shouldAddFoodPortionToMeal() {

        // Given
        Long mealId = 1L;
        Long foodId = 10L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

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
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

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

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

        MealRecord meal = mock(MealRecord.class);
        FoodPortionRecord portion = mock(FoodPortionRecord.class);

        when(meal.getId()).thenReturn(mealId);
        when(portion.getId()).thenReturn(portionId);

        when(meal.getFoodPortions()).thenReturn(List.of(portion));

        dailyRecord.addMeal(meal);

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.of(dailyRecord));

        // When
        service.removePortion(
                dailyRecordId,
                mealId,
                portionId
        );

        // Then
        verify(meal).removeFoodPortion(portion);
        verify(dailyRecordRepository).save(dailyRecord);
    }

    @Test
    void shouldThrowWhenMealNotFoundInRemovePortion() {

        // Given
        Long dailyRecordId = 1L;
        Long mealId = 10L;
        Long portionId = 100L;

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

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

        PatientProfile patient = patientProfile();
        DailyRecord dailyRecord = DailyRecord.of(patient, LocalDate.now());

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

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
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

        NutritionPlan plan = createPlan(
                2000,
                150,
                200,
                70
        );

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

        DailyNutritionComparisonDTO day =
                result.days().get(0);

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
                DailyRecord.of(patient, LocalDate.now());

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
    void shouldThrowWhenDailyRecordNotFoundInRemovePortion() {

        Long dailyRecordId = 1L;
        Long mealId = 10L;
        Long portionId = 100L;

        when(dailyRecordRepository.findById(dailyRecordId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.removePortion(
                        dailyRecordId,
                        mealId,
                        portionId
                )
        );

        verify(dailyRecordRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateTodayRecordWithMealsFromActivePlan() {

        // Given
        Long patientId = 1L;

        LocalDate today = LocalDate.now();

        PatientProfile patient = mock(PatientProfile.class);
        NutritionPlan activePlan = mock(NutritionPlan.class);
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PlanMeal planMeal = mock(PlanMeal.class);

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

        // When
        DailyRecordResponseDTO result =
                service.getOrCreateToday(patientId);

        // Then
        assertNotNull(result);

        verify(dailyRecordRepository)
                .save(any(DailyRecord.class));

        verify(dailyPlan)
                .getMeals();

    }

    @Test
    void shouldCreateRecordWithoutMealsWhenTodayDailyPlanDoesNotExist() {

        // Given
        Long patientId = 1L;

        User user = generateUser();
        PatientProfile patient = user.getPatientProfile();

        NutritionPlan plan = mock(NutritionPlan.class);

        DailyPlan otherDay = mock(DailyPlan.class);

        when(otherDay.getDayOfWeek())
                .thenReturn(
                        LocalDate.now()
                                .getDayOfWeek()
                                .plus(1)
                );

        when(plan.getWeek())
                .thenReturn(List.of(otherDay));

        when(patientProfileService.findById(patientId))
                .thenReturn(patient);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DailyRecordResponseDTO result =
                service.getOrCreateToday(patientId);

        // Then
        assertNotNull(result);

        verify(dailyRecordRepository)
                .save(any(DailyRecord.class));
    }

    @ParameterizedTest
    @EnumSource(MealType.class)
    void shouldCreateTodayRecordWithMealsFromActivePlan(MealType mealType) {

        // Given
        Long patientId = 1L;

        LocalDate today = LocalDate.now();

        PatientProfile patient = mock(PatientProfile.class);
        NutritionPlan activePlan = mock(NutritionPlan.class);
        DailyPlan dailyPlan = mock(DailyPlan.class);
        PlanMeal planMeal = mock(PlanMeal.class);

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

        // When
        DailyRecordResponseDTO result =
                service.getOrCreateToday(patientId);

        // Then
        assertNotNull(result);

        verify(dailyRecordRepository)
                .save(any(DailyRecord.class));
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

        DailyRecord dailyRecord = mock(DailyRecord.class);

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

        DailyRecord dailyRecord = mock(DailyRecord.class);
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
    void shouldReturnZeroConsumedMacrosWhenDayHasNoRecord() {

        // Given
        Long patientId = 1L;

        NutritionPlan plan = mock(NutritionPlan.class);

        when(plan.getDailyCalories()).thenReturn(2000);
        when(plan.getProteinGrams()).thenReturn(150);
        when(plan.getCarbGrams()).thenReturn(250);
        when(plan.getFatGrams()).thenReturn(70);

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                patientId,
                from,
                to))
                .thenReturn(List.of());

        // When
        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        from,
                        to
                );

        // Then
        assertEquals(1, result.days().size());

        DailyNutritionComparisonDTO day =
                result.days().getFirst();

        assertAll(
                () -> assertEquals(0, day.consumedCalories()),
                () -> assertEquals(0, day.consumedProtein()),
                () -> assertEquals(0, day.consumedCarbs()),
                () -> assertEquals(0, day.consumedFat()),
                () -> assertEquals(0.0, day.adherencePercentage())
        );
    }

    @Test
    void shouldReturnZeroAdherenceWhenTargetCaloriesIsZero() {

        // Given
        Long patientId = 1L;

        NutritionPlan plan = mock(NutritionPlan.class);

        when(plan.getDailyCalories()).thenReturn(0);
        when(plan.getProteinGrams()).thenReturn(150);
        when(plan.getCarbGrams()).thenReturn(250);
        when(plan.getFatGrams()).thenReturn(70);

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        when(nutritionPlanService.findActivePlan(patientId))
                .thenReturn(Optional.of(plan));

        when(dailyRecordRepository.findByPatient_IdAndDateBetween(
                patientId,
                from,
                to))
                .thenReturn(List.of());

        // When
        NutritionComparisonReportDTO result =
                service.getNutritionComparison(
                        patientId,
                        from,
                        to
                );

        // Then
        DailyNutritionComparisonDTO day =
                result.days().get(0);

        assertAll(
                () -> assertEquals(0, day.targetCalories()),
                () -> assertEquals(0, day.consumedCalories()),
                () -> assertEquals(0.0, day.adherencePercentage())
        );
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

        user.setBirthDate(LocalDate.now().minusYears(30));

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


}