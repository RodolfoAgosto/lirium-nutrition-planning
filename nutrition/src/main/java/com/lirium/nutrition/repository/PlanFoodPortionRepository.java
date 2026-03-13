package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanFoodPortionRepository extends JpaRepository<PlanFoodPortion, Long> {

    List<PlanFoodPortion> findByMeal(PlanMeal meal);

    List<PlanFoodPortion> findByFood(Food food);

    List<PlanFoodPortion> findByMealAndFood(PlanMeal meal, Food food);

    List<PlanFoodPortion> findByPlanMealId(Long planMealId);

}