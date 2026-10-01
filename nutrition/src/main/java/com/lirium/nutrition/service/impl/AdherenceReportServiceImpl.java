package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.AdherenceSummaryDTO;
import com.lirium.nutrition.dto.response.DailyAdherenceDTO;
import com.lirium.nutrition.exception.PatientProfileNotFoundException;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.entity.MealRecord;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.AdherenceReportService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      throw new PatientProfileNotFoundException(patientId);
    }

    LocalDate earliestStartDate =
        nutritionPlanRepository
            .findFirstByPatientProfile_IdOrderByStartDateAsc(patientId)
            .map(NutritionPlan::getStartDate)
            .orElseThrow(() -> new IllegalStateException("Patient has no nutrition plan history"));

    if (from.isBefore(earliestStartDate)) {
      throw new IllegalArgumentException(
          "Requested start date ('from') cannot be prior to the patient's first plan start date ("
              + earliestStartDate
              + ")");
    }

    // Retrieve records and map to Map<LocalDate, DailyRecord> O(1)
    List<DailyRecord> records =
        dailyRecordRepository.findByPatient_IdAndDateBetweenWithMeals(patientId, from, to);

    Map<LocalDate, DailyRecord> recordByDate =
        records.stream()
            .collect(
                Collectors.toMap(
                    DailyRecord::getDate, r -> r, (existing, replacement) -> existing));
    int expectedMealsPerDay = MealType.values().length; // 5

    List<DailyAdherenceDTO> days =
        from.datesUntil(to.plusDays(1))
            .map(date -> measureDay(date, recordByDate.get(date), expectedMealsPerDay))
            .toList();

    return new AdherenceReportDTO(from, to, summarize(days), days);
  }

  private DailyAdherenceDTO measureDay(LocalDate date, DailyRecord record, int expectedMeals) {
    if (record == null) {
      return new DailyAdherenceDTO(date, false, expectedMeals, null, null);
    }

    int followed = (int) record.getMeals().stream().filter(MealRecord::followsPlan).count();
    int modified = record.getMeals().size() - followed;

    return new DailyAdherenceDTO(date, true, expectedMeals, followed, modified);
  }

  private AdherenceSummaryDTO summarize(List<DailyAdherenceDTO> days) {
    int expected = days.stream().mapToInt(DailyAdherenceDTO::expectedMeals).sum();

    List<DailyAdherenceDTO> recorded = days.stream().filter(DailyAdherenceDTO::hasRecord).toList();
    int followed = recorded.stream().mapToInt(DailyAdherenceDTO::followedMeals).sum();
    int expectedOnRecorded = recorded.stream().mapToInt(DailyAdherenceDTO::expectedMeals).sum();

    Double adherenceOnRecordedDays =
        expectedOnRecorded > 0 ? percent(followed, expectedOnRecorded) : null;

    return new AdherenceSummaryDTO(
        days.size(),
        recorded.size(),
        expected,
        followed,
        expected > 0 ? percent(followed, expected) : 0.0,
        adherenceOnRecordedDays);
  }

  private static double percent(int part, int total) {
    return Math.round(part * 1000.0 / total) / 10.0;
  }
}
