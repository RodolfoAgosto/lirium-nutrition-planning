package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.FoodPortionRecord;
import com.lirium.nutrition.model.entity.MealRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodPortionRecordRepository extends JpaRepository<FoodPortionRecord, Long> {

  List<FoodPortionRecord> findByMeal(MealRecord meal);

  boolean existsByFoodId(Long foodId);
}
