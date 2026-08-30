package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.DailyNutritionComparisonDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.dto.response.NutritionComparisonReportDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.DailyRecordMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.MealRecordRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.DailyRecordService;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.NutritionPlanService;
import com.lirium.nutrition.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRecordServiceImpl implements DailyRecordService {

  private static final String MEAL_RECORD = "MealRecord";

  private final DailyRecordRepository dailyRecordRepository;
  private final PatientProfileService patientProfileService;
  private final NutritionPlanService nutritionPlanService;
  private final FoodService foodService;
  private final MealRecordRepository mealRecordRepository;
  private final PatientProfileRepository patientProfileRepository;
  private final Clock clock;

  @Transactional
  public DailyRecordResponseDTO getOrCreateForDate(Long patientId, LocalDate date) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now(clock);

    if (targetDate.isAfter(LocalDate.now(clock))) {
      throw new IllegalArgumentException("Cannot create daily records for future dates");
    }

    return dailyRecordRepository
        .findByPatient_IdAndDate(patientId, targetDate)
        .map(DailyRecordMapper::toResponse)
        .orElseGet(
            () -> {
              return createRecordForDate(patientId, targetDate);
            });
  }

  private DailyRecordResponseDTO createRecordForDate(Long patientId, LocalDate targetDate) {

    PatientProfile patient = patientProfileService.findById(patientId);

    NutritionPlan activePlan =
        nutritionPlanService
            .findActivePlan(patientId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Patient has no active nutrition plan. Cannot create daily record."));

    if (activePlan.getStartDate() != null && targetDate.isBefore(activePlan.getStartDate())) {
      throw new IllegalArgumentException(
          "Cannot create daily record for a date ("
              + targetDate
              + ") prior to active nutrition plan start date ("
              + activePlan.getStartDate()
              + ")");
    }

    DailyRecord dailyRecord = DailyRecord.of(patient, targetDate);

    activePlan.getWeek().stream()
        .filter(dp -> dp.getDayOfWeek() == targetDate.getDayOfWeek())
        .findFirst()
        .ifPresent(
            dailyPlan ->
                dailyPlan
                    .getMeals()
                    .forEach(
                        planMeal -> {
                          MealRecord meal =
                              MealRecord.fromPlan(
                                  planMeal,
                                  targetDate.atTime(defaultTimeFor(planMeal.getType())),
                                  dailyRecord);
                          dailyRecord.addMeal(meal);
                        }));

    DailyRecord savedRecord = dailyRecordRepository.save(dailyRecord);
    return DailyRecordMapper.toResponse(savedRecord);
  }

  private LocalTime defaultTimeFor(MealType type) {
    return switch (type) {
      case BREAKFAST -> LocalTime.of(8, 0);
      case MID_MORNING -> LocalTime.of(10, 30);
      case LUNCH -> LocalTime.of(13, 0);
      case SNACK -> LocalTime.of(17, 0);
      case DINNER -> LocalTime.of(20, 0);
    };
  }

  @Override
  public DailyRecordResponseDTO getById(Long id) {
    return dailyRecordRepository
        .findById(id)
        .map(DailyRecordMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("DailyRecord", id));
  }

  @Override
  public List<DailyRecordResponseDTO> getByPatient(Long patientId) {

    if (!patientProfileRepository.existsById(patientId)) {
      throw new ResourceNotFoundException("Patient", patientId);
    }

    return dailyRecordRepository.findByPatient_IdOrderByDateDesc(patientId).stream()
        .map(DailyRecordMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public MealRecordResponseDTO updateMeal(Long mealRecordId, MealRecordUpdateRequestDTO request) {
    DailyRecord dailyRecord =
        dailyRecordRepository
            .findByMealRecordId(mealRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("DailyRecord for meal", mealRecordId));

    MealRecord meal =
        dailyRecord.getMeals().stream()
            .filter(m -> m.getId().equals(mealRecordId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(MEAL_RECORD, mealRecordId));

    if (request.notes() != null) {
      meal.markAsOverridden(request.notes());
    }

    dailyRecordRepository.save(dailyRecord);
    return DailyRecordMapper.toMealResponse(meal);
  }

  @Override
  @Transactional
  public MealRecordResponseDTO addPortion(Long mealRecordId, FoodPortionAddRequestDTO request) {

    log.info("Adding food portion mealRecordId={} foodId={}", mealRecordId, request.foodId());

    DailyRecord dailyRecord =
        dailyRecordRepository
            .findByMealRecordId(mealRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("DailyRecord for meal", mealRecordId));

    MealRecord meal =
        dailyRecord.getMeals().stream()
            .filter(m -> m.getId().equals(mealRecordId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(MEAL_RECORD, mealRecordId));

    Food food = foodService.findEntityById(request.foodId());

    meal.addFoodPortion(food, request.quantity(), request.unit());

    dailyRecordRepository.save(dailyRecord);
    return DailyRecordMapper.toMealResponse(meal);
  }

  @Override
  @Transactional
  public void removePortion(Long dailyRecordId, Long mealRecordId, Long portionId) {
    DailyRecord dailyRecord =
        dailyRecordRepository
            .findById(dailyRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("DailyRecord", dailyRecordId));

    NutritionPlan activePlan =
        nutritionPlanService
            .findActivePlan(dailyRecord.getPatient().getId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Patient has no active nutrition plan. Cannot modify daily record."));

    if (dailyRecord.getDate().isBefore(activePlan.getStartDate())) {
      throw new IllegalArgumentException(
          "Cannot modify records prior to the active plan start date: "
              + activePlan.getStartDate());
    }

    MealRecord meal =
        dailyRecord.getMeals().stream()
            .filter(m -> m.getId().equals(mealRecordId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(MEAL_RECORD, mealRecordId));

    FoodPortionRecord portion =
        meal.getFoodPortions().stream()
            .filter(p -> p.getId().equals(portionId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("FoodPortionRecord", portionId));

    meal.markAsOverridden();
    meal.removeFoodPortion(portion);
    dailyRecordRepository.save(dailyRecord);
  }

  @Override
  public NutritionComparisonReportDTO getNutritionComparison(
      Long patientId, LocalDate from, LocalDate to) {

    validateDateRange(from, to);
    validatePatientExists(patientId);

    NutritionPlan activePlan =
        nutritionPlanService
            .findActivePlan(patientId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Active nutrition plan not found for patient with id:", patientId));

    LocalDate effectiveFrom = getEffectiveFrom(activePlan, from);

    if (effectiveFrom.isAfter(to)) {
      return new NutritionComparisonReportDTO(from, to, List.of());
    }

    List<DailyRecord> records =
        dailyRecordRepository.findByPatient_IdAndDateBetween(patientId, effectiveFrom, to);

    List<DailyNutritionComparisonDTO> days =
        effectiveFrom
            .datesUntil(to.plusDays(1))
            .map(date -> buildDailyComparison(date, records, activePlan))
            .toList();

    return new NutritionComparisonReportDTO(effectiveFrom, to, days);
  }

  @Override
  public boolean isDailyRecordOwnedByUser(Long dailyRecordId, Long userId) {
    return dailyRecordRepository
        .findById(dailyRecordId)
        .map(dailyRecord -> dailyRecord.getPatient().getUser().getId().equals(userId))
        .orElse(false);
  }

  @Override
  public boolean isMealRecordOwnedByUser(Long mealRecordId, Long userId) {
    return mealRecordRepository
        .findById(mealRecordId)
        .map(
            mealRecord -> mealRecord.getDailyRecord().getPatient().getUser().getId().equals(userId))
        .orElse(false);
  }

  @Override
  public boolean existsForPatientAndDate(Long patientId, LocalDate date) {
    return dailyRecordRepository.existsByPatient_IdAndDate(patientId, date);
  }

  private void validateDateRange(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Date range is required");
    }

    if (from.isAfter(to)) {
      throw new IllegalArgumentException("The 'from' date cannot be after the 'to' date");
    }
  }

  private void validatePatientExists(Long patientId) {
    if (!patientProfileRepository.existsById(patientId)) {
      throw new ResourceNotFoundException("Patient", patientId);
    }
  }

  private LocalDate getEffectiveFrom(NutritionPlan activePlan, LocalDate from) {
    LocalDate planStart = activePlan.getStartDate();

    if (planStart != null && from.isBefore(planStart)) {
      return planStart;
    }

    return from;
  }

  private DailyNutritionComparisonDTO buildDailyComparison(
      LocalDate date, List<DailyRecord> records, NutritionPlan activePlan) {

    Optional<DailyRecord> record =
        records.stream().filter(r -> r.getDate().equals(date)).findFirst();

    NutritionTotals consumed = calculateConsumedNutrition(record);

    int targetCal = activePlan.getDailyCalories();
    double adherence = calculateAdherence(targetCal, consumed.calories());

    return new DailyNutritionComparisonDTO(
        date,
        targetCal,
        consumed.calories(),
        activePlan.getProteinGrams(),
        consumed.protein(),
        activePlan.getCarbGrams(),
        consumed.carbs(),
        activePlan.getFatGrams(),
        consumed.fat(),
        Math.round(adherence * 10.0) / 10.0);
  }

  private record NutritionTotals(int calories, int protein, int carbs, int fat) {}

  private NutritionTotals calculateConsumedNutrition(Optional<DailyRecord> record) {

    if (record.isEmpty()) {
      return new NutritionTotals(0, 0, 0, 0);
    }

    int calories = 0;
    int protein = 0;
    int carbs = 0;
    int fat = 0;

    for (MealRecord meal : record.get().getMeals()) {
      for (FoodPortionRecord portion : meal.getFoodPortions()) {
        calories += portion.calories().amount();
        protein += portion.protein().grams();
        carbs += portion.carbs().amount();
        fat += portion.fat().amount();
      }
    }

    return new NutritionTotals(calories, protein, carbs, fat);
  }

  private double calculateAdherence(int targetCalories, int consumedCalories) {
    if (targetCalories <= 0) {
      return 0.0;
    }

    return Math.min(100.0, consumedCalories * 100.0 / targetCalories);
  }
}
