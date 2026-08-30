package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.MealType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanMealRepository extends JpaRepository<PlanMeal, Long> {

  List<PlanMeal> findByDailyPlan(DailyPlan dailyPlan);

  List<PlanMeal> findByDailyPlanId(Long dailyPlanId);

  List<PlanMeal> findByDailyPlanAndType(DailyPlan dailyPlan, MealType type);

  @Query(
      """
        SELECT COUNT(pm) > 0
        FROM PlanMeal pm
        WHERE pm.id = :mealId
          AND pm.dailyPlan.nutritionPlan.patientProfile.user.id = :userId
    """)
  boolean existsByIdAndUserId(@Param("mealId") Long mealId, @Param("userId") Long userId);
}
