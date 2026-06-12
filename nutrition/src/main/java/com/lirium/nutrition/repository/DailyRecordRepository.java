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

    Optional<DailyRecord> findByPatient_IdAndDate(Long patientId, LocalDate date);

    List<DailyRecord> findByPatient_IdOrderByDateDesc(Long patientId);

    List<DailyRecord> findByPatient_IdAndDateBetween(Long patientId, LocalDate from, LocalDate to);

    @Query("SELECT DISTINCT dr FROM DailyRecord dr LEFT JOIN FETCH dr.meals WHERE dr.patient.id = :patientId AND dr.date BETWEEN :from AND :to")
    List<DailyRecord> findByPatient_IdAndDateBetweenWithMeals(@Param("patientId") Long patientId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    // Para navegar desde meal al aggregate root
    @Query("SELECT dr FROM DailyRecord dr JOIN dr.meals m WHERE m.id = :mealRecordId")
    Optional<DailyRecord> findByMealRecordId(@Param("mealRecordId") Long mealRecordId);

}

