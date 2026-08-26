package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyAdherenceDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.AdherenceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdherenceReportServiceImpl implements AdherenceReportService {

    private final DailyRecordRepository dailyRecordRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final NutritionPlanRepository nutritionPlanRepository;

    @Override
    public AdherenceReportDTO getAdherence(Long patientId, LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The 'from' date must be prior or equal to 'to' date");
        }

        if (!patientProfileRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        LocalDate earliestStartDate = nutritionPlanRepository.findFirstByPatientProfile_IdOrderByStartDateAsc(patientId)
                .map(NutritionPlan::getStartDate)
                .orElseThrow(() -> new IllegalStateException("Patient has no nutrition plan history"));

        if (from.isBefore(earliestStartDate)) {
            throw new IllegalArgumentException(
                    "Requested start date ('from') cannot be prior to the patient's first plan start date (" + earliestStartDate + ")"
            );
        }

        // Retrieve records and map to Map<LocalDate, DailyRecord> O(1)
        List<DailyRecord> records = dailyRecordRepository
                .findByPatient_IdAndDateBetweenWithMeals(patientId, from, to);

        Map<LocalDate, DailyRecord> recordByDate = records.stream()
                .collect(Collectors.toMap(
                        DailyRecord::getDate,
                        r -> r,
                        (existing, replacement) -> existing
                ));

        long totalDays = ChronoUnit.DAYS.between(from, to) + 1;
        int expectedMealsPerDay = MealType.values().length; // 5
        int totalExpected = (int) (totalDays * expectedMealsPerDay);

        // Generation of the daily breakdown
        List<DailyAdherenceDTO> daily = from.datesUntil(to.plusDays(1))
                .map(date -> {
                    DailyRecord record = recordByDate.get(date);

                    int recorded = 0;
                    boolean present = false;

                    if (record != null) {
                        present = true;
                        recorded = (int) record.getMeals().stream()
                                .filter(mr -> !mr.isOverridden())
                                .count();
                    }

                    return new DailyAdherenceDTO(
                            date,
                            expectedMealsPerDay,
                            recorded,
                            present
                    );
                })
                .toList();

        int totalRecorded = daily.stream()
                .mapToInt(DailyAdherenceDTO::recordedMeals)
                .sum();

        double adherence = totalExpected > 0
                ? (totalRecorded * 100.0 / totalExpected)
                : 0.0;

        return new AdherenceReportDTO(
                from,
                to,
                totalExpected,
                totalRecorded,
                Math.round(adherence * 10.0) / 10.0,
                daily
        );
    }
}
