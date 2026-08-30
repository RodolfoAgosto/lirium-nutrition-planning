package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("planMealSecurity")
@RequiredArgsConstructor
public class PlanMealSecurity {

  private final PlanMealRepository planMealRepository;
  private final DailyPlanRepository dailyPlanRepository;

  public boolean isOwner(Long mealId, Authentication authentication) {
    if (mealId == null || authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    User principal = (User) authentication.getPrincipal();
    return planMealRepository.existsByIdAndUserId(mealId, principal.getId());
  }

  public boolean isPlanDayOwner(Long planDayId, Authentication authentication) {
    if (planDayId == null || authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    User principal = (User) authentication.getPrincipal();

    return dailyPlanRepository.existsByIdAndNutritionPlan_PatientProfile_User_Id(
        planDayId, principal.getId());
  }
}
