package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.MealRecord;
import com.lirium.nutrition.model.enums.MealType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

  List<MealRecord> findByType(MealType type);

  List<MealRecord> findByEatenAtBetween(LocalDateTime start, LocalDateTime end);

  List<MealRecord> findByTypeAndEatenAtBetween(
      MealType type, LocalDateTime start, LocalDateTime end);

  long deleteByEatenAtBefore(LocalDateTime dateTime);
}
