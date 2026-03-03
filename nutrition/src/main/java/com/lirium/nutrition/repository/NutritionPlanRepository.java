package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByName(String name);

    List<NutritionPlan> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    List<NutritionPlan> findByTargetGoal(GoalType goalType);

    void deleteByEndDateBefore(LocalDate date);
}