package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {
    Optional<DailyRecord> findByPatientIdAndDate(Long patientId, LocalDate date);
    List<DailyRecord> findByPatientIdOrderByDateDesc(Long patientId);
    List<DailyRecord> findByPatientIdAndDateBetween(Long patientId, LocalDate from, LocalDate to);

    // Nuevo — para navegar desde meal al aggregate root
    @Query("SELECT dr FROM DailyRecord dr JOIN dr.meals m WHERE m.id = :mealRecordId")
    Optional<DailyRecord> findByMealRecordId(@Param("mealRecordId") Long mealRecordId);

}

