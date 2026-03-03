package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.FoodPortionRecord;
import com.lirium.nutrition.model.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodPortionRecordRepository extends JpaRepository<FoodPortionRecord, Long> {

    List<FoodPortionRecord> findByMeal(MealRecord meal);

}