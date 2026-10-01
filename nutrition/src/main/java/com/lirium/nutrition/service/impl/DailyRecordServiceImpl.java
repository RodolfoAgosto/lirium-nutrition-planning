package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.exception.*;
import com.lirium.nutrition.mapper.DailyRecordMapper;
import com.lirium.nutrition.mapper.NutritionComparisonMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.NutrientBudget;
import com.lirium.nutrition.model.valueobject.NutritionScore;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.MealRecordRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.DailyRecordService;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.NutritionPlanService;
import com.lirium.nutrition.service.PatientProfileService;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        .orElseThrow(() -> new DailyRecordNotFoundException(id));
  }

  @Override
  public List<DailyRecordResponseDTO> getByPatient(Long patientId) {

    if (!patientProfileRepository.existsById(patientId)) {
      throw new PatientProfileNotFoundException(patientId);
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
            .orElseThrow(() -> new DailyRecordNotFoundException(mealRecordId));

    MealRecord meal =
        dailyRecord.getMeals().stream()
            .filter(m -> m.getId().equals(mealRecordId))
            .findFirst()
            .orElseThrow(() -> new MealRecordNotFoundException(mealRecordId));

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
            .orElseThrow(
                () ->
                    new DailyRecordNotFoundException(
                        "DailyRecord not found for meal" + mealRecordId));

    MealRecord meal =
        dailyRecord.getMeals().stream()
            .filter(m -> m.getId().equals(mealRecordId))
            .findFirst()
            .orElseThrow(() -> new MealRecordNotFoundException(mealRecordId));

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
            .orElseThrow(() -> new DailyRecordNotFoundException(dailyRecordId));

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
            .orElseThrow(() -> new MealRecordNotFoundException(mealRecordId));

    FoodPortionRecord portion =
        meal.getFoodPortions().stream()
            .filter(p -> p.getId().equals(portionId))
            .findFirst()
            .orElseThrow(() -> new FoodPortionRecordNotFoundException(portionId));

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
                    new NutritionPlanNotFoundException(
                        "Active nutrition plan not found for patient with id:" + patientId));

    LocalDate effectiveFrom = getEffectiveFrom(activePlan, from);

    if (effectiveFrom.isAfter(to)) {
      return new NutritionComparisonReportDTO(
          from,
          to,
          NutritionComparisonMapper.targetsOf(activePlan),
          new NutritionComparisonSummaryDTO(0, 0, null, null),
          List.of());
    }

    List<DailyRecord> records =
        dailyRecordRepository.findByPatient_IdAndDateBetween(patientId, effectiveFrom, to);

    Map<DayOfWeek, NutrientBudget> plannedByDay = new EnumMap<>(DayOfWeek.class);
    for (DayOfWeek day : DayOfWeek.values()) {
      plannedByDay.put(day, activePlan.plannedNutrientsFor(day).orElse(NutrientBudget.ZERO));
    }

    List<DailyComparison> comparisons =
        effectiveFrom
            .datesUntil(to.plusDays(1))
            .map(date -> compareDay(date, records, plannedByDay.get(date.getDayOfWeek())))
            .toList();

    return new NutritionComparisonReportDTO(
        effectiveFrom,
        to,
        NutritionComparisonMapper.targetsOf(activePlan),
        summarize(comparisons),
        comparisons.stream().map(DailyRecordServiceImpl::toDayDto).toList());
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
      throw new PatientProfileNotFoundException(patientId);
    }
  }

  private LocalDate getEffectiveFrom(NutritionPlan activePlan, LocalDate from) {
    LocalDate planStart = activePlan.getStartDate();

    if (planStart != null && from.isBefore(planStart)) {
      return planStart;
    }

    return from;
  }

  private DailyComparison compareDay(
      LocalDate date, List<DailyRecord> records, NutrientBudget planned) {

    Optional<DailyRecord> record =
        records.stream().filter(r -> r.getDate().equals(date)).findFirst();

    if (record.isEmpty()) {
      return new DailyComparison(date, planned, null, null);
    }

    NutrientBudget consumed = calculateConsumedNutrition(record.get());
    return new DailyComparison(
        date, planned, consumed, NutritionScore.of(planned, consumed).orElse(null));
  }

  private NutritionComparisonSummaryDTO summarize(List<DailyComparison> days) {

    List<NutrientBudget> consumed =
        days.stream().map(DailyComparison::consumed).filter(Objects::nonNull).toList();

    List<NutritionScore> scores =
        days.stream().map(DailyComparison::score).filter(Objects::nonNull).toList();

    return new NutritionComparisonSummaryDTO(
        days.size(),
        consumed.size(),
        NutritionScore.average(scores).map(NutritionComparisonMapper::toScore).orElse(null),
        NutrientBudget.average(consumed).map(NutritionComparisonMapper::toNutrients).orElse(null));
  }

  private static DailyNutritionComparisonDTO toDayDto(DailyComparison day) {
    return new DailyNutritionComparisonDTO(
        day.date(),
        day.consumed() != null,
        NutritionComparisonMapper.toNutrients(day.planned()),
        day.consumed() == null ? null : NutritionComparisonMapper.toNutrients(day.consumed()),
        day.score() == null ? null : NutritionComparisonMapper.toScore(day.score()));
  }

  /** One day of the report in domain terms: consumed and score are null when there is no data. */
  private record DailyComparison(
      LocalDate date, NutrientBudget planned, NutrientBudget consumed, NutritionScore score) {}

  private NutrientBudget calculateConsumedNutrition(DailyRecord record) {
    return record.getMeals().stream()
        .flatMap(meal -> meal.getFoodPortions().stream())
        .map(
            portion ->
                new NutrientBudget(
                    portion.calories(), portion.carbs(), portion.fat(), portion.protein()))
        .reduce(NutrientBudget.ZERO, NutrientBudget::add);
  }
}
