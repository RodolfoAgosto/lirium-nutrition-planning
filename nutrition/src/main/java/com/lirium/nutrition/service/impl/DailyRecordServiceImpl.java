package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.AddFoodPortionRequestDTO;
import com.lirium.nutrition.dto.request.MealRecordUpdateRequestDTO;
import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.DailyRecordMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.repository.*;
import com.lirium.nutrition.service.DailyRecordService;
import com.lirium.nutrition.service.FoodService;
import com.lirium.nutrition.service.NutritionPlanService;
import com.lirium.nutrition.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRecordServiceImpl implements DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;
    private final PatientProfileService patientProfileService;
    private final NutritionPlanService nutritionPlanService;
    private final FoodService foodService;

    @Override
    @Transactional
    public DailyRecordResponseDTO getOrCreateToday(Long patientId) {
        LocalDate today = LocalDate.now();
        return dailyRecordRepository
                .findByPatientIdAndDate(patientId, today)
                .map(DailyRecordMapper::toResponse)
                .orElseGet(() -> createTodayRecord(patientId, today));
    }

    private DailyRecordResponseDTO createTodayRecord(Long patientId, LocalDate today) {

        PatientProfile patient = patientProfileService.findByUserId(patientId);

        // Si no tiene plan activo no puede registrar comidas
        NutritionPlan activePlan = nutritionPlanService.findActivePlan(patientId)
                .orElseThrow(() -> new IllegalStateException(
                        "Patient has no active nutrition plan. Cannot create daily record."));

        DailyRecord dailyRecord = DailyRecord.of(patient, today);

        activePlan.getWeek().stream()
                .filter(dp -> dp.getDayOfWeek() == today.getDayOfWeek())
                .findFirst()
                .ifPresent(dailyPlan ->
                        dailyPlan.getMeals().forEach(planMeal -> {
                            MealRecord meal = MealRecord.fromPlan(
                                    planMeal,
                                    today.atTime(defaultTimeFor(planMeal.getType())),
                                    dailyRecord
                            );
                            dailyRecord.addMeal(meal);
                        })
                );

        dailyRecordRepository.save(dailyRecord);
        return DailyRecordMapper.toResponse(dailyRecord);
    }

    private LocalTime defaultTimeFor(MealType type) {
        return switch (type) {
            case BREAKFAST   -> LocalTime.of(8, 0);
            case MID_MORNING -> LocalTime.of(10, 30);
            case LUNCH       -> LocalTime.of(13, 0);
            case SNACK       -> LocalTime.of(17, 0);
            case DINNER      -> LocalTime.of(20, 0);
        };
    }

    @Override
    public DailyRecordResponseDTO getById(Long id) {
        return dailyRecordRepository.findById(id)
                .map(DailyRecordMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("DailyRecord", id));
    }

    @Override
    public List<DailyRecordResponseDTO> getByPatient(Long patientId) {
        return dailyRecordRepository
                .findByPatientIdOrderByDateDesc(patientId)
                .stream()
                .map(DailyRecordMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MealRecordResponseDTO updateMeal(Long mealRecordId, MealRecordUpdateRequestDTO request) {
        DailyRecord dailyRecord = dailyRecordRepository
                .findByMealRecordId(mealRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyRecord for meal", mealRecordId));

        MealRecord meal = dailyRecord.getMeals().stream()
                .filter(m -> m.getId().equals(mealRecordId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("MealRecord", mealRecordId));

        if (request.notes() != null) {
            meal.markAsOverridden(request.notes());
        }

        dailyRecordRepository.save(dailyRecord);
        return DailyRecordMapper.toMealResponse(meal);
    }

    @Override
    @Transactional
    public MealRecordResponseDTO addPortion(Long mealRecordId, AddFoodPortionRequestDTO request) {
        DailyRecord dailyRecord = dailyRecordRepository
                .findByMealRecordId(mealRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyRecord for meal", mealRecordId));

        MealRecord meal = dailyRecord.getMeals().stream()
                .filter(m -> m.getId().equals(mealRecordId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("MealRecord", mealRecordId));

        Food food = foodService.findEntityById(request.foodId());
        meal.addFoodPortion(food, request.quantity(), request.unit());

        dailyRecordRepository.save(dailyRecord);
        return DailyRecordMapper.toMealResponse(meal);
    }

    @Override
    @Transactional
    public void removePortion(Long dailyRecordId, Long mealRecordId, Long portionId) {
        DailyRecord dailyRecord = dailyRecordRepository.findById(dailyRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyRecord", dailyRecordId));

        MealRecord meal = dailyRecord.getMeals().stream()
                .filter(m -> m.getId().equals(mealRecordId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("MealRecord", mealRecordId));

        FoodPortionRecord portion = meal.getFoodPortions().stream()
                .filter(p -> p.getId().equals(portionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodPortionRecord", portionId));

        meal.removeFoodPortion(portion);
        dailyRecordRepository.save(dailyRecord);
    }
}