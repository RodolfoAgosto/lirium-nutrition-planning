package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.FoodTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanTemplateRepository extends JpaRepository<NutritionPlanTemplate, Long> {

    Optional<NutritionPlanTemplate> findByName(String name);

    List<NutritionPlanTemplate> findByTargetGoal(GoalType targetGoal);

    List<NutritionPlanTemplate> findByExcludedTags(FoodTag tag);

    void deleteByName(String name);

}