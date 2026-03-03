package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanMealRepository extends JpaRepository<PlanMeal, Long> {

    List<PlanMeal> findByDailyPlan(DailyPlan dailyPlan);

    List<PlanMeal> findByDailyPlanAndType(DailyPlan dailyPlan, MealType type);

}