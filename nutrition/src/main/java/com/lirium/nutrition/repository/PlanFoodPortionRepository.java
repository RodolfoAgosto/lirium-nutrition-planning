package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanFoodPortionRepository extends JpaRepository<PlanFoodPortion, Long> {

  List<PlanFoodPortion> findByMeal(PlanMeal meal);

  List<PlanFoodPortion> findByMealId(Long mealId);

  List<PlanFoodPortion> findByFood(Food food);

  List<PlanFoodPortion> findByMealAndFood(PlanMeal meal, Food food);

  boolean existsByMeal_IdAndFood_Id(Long planMealId, Long foodId);

  boolean existsByFoodId(Long foodId);
}
