package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByName(String name);

    List<NutritionPlan> findByTargetGoal(GoalType goalType);

    long deleteByEndDateBefore(LocalDate date);

    Optional<NutritionPlan> findByPatientProfileIdAndStatus(Long patientId, PlanStatus status);

    Optional<NutritionPlan> findByPatientProfileUserIdAndStatus(Long userId, PlanStatus status);

    List<NutritionPlan> findByPatientProfileIdOrderByStartDateDesc(Long patientId);

    boolean existsByPatientProfileIdAndStatus(Long patientId, PlanStatus status);

}