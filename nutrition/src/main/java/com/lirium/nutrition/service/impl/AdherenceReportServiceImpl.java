package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;
import com.lirium.nutrition.dto.response.DailyAdherenceDTO;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.service.AdherenceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdherenceReportServiceImpl implements AdherenceReportService {

    private final DailyRecordRepository dailyRecordRepository;

    @Override
    public AdherenceReportDTO getAdherence(Long patientId, LocalDate from, LocalDate to) {

        List<DailyRecord> records = dailyRecordRepository
                .findByPatientIdAndDateBetween(patientId, from, to);

        long totalDays = ChronoUnit.DAYS.between(from, to) + 1;
        int expectedMealsPerDay = MealType.values().length; // 5
        int totalExpected = (int)(totalDays * expectedMealsPerDay);

        List<DailyAdherenceDTO> daily = from.datesUntil(to.plusDays(1))
                .map(date -> {
                    Optional<DailyRecord> record = records.stream()
                            .filter(r -> r.getDate().equals(date))
                            .findFirst();

                    int recorded = record
                            .map(r -> r.getMeals().size())
                            .orElse(0);

                    return new DailyAdherenceDTO(
                            date,
                            expectedMealsPerDay,
                            recorded,
                            record.isPresent()
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
