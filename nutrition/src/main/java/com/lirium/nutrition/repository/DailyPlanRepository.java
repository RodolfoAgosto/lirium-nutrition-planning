package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {

    List<DailyPlan> findByNutritionPlan(NutritionPlan nutritionPlan);

    Optional<DailyPlan> findByNutritionPlanAndDay(NutritionPlan nutritionPlan, DayOfWeek day);

    void deleteByNutritionPlan(NutritionPlan nutritionPlan);

}