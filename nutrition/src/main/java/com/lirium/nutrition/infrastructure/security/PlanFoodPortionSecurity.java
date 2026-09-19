package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PlanFoodPortionRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("planFoodPortionSecurity")
@RequiredArgsConstructor
public class PlanFoodPortionSecurity {

  private final PlanFoodPortionRepository portionRepository;
  private final PlanMealRepository mealRepository;

  public boolean isPortionOwner(Long portionId, Authentication authentication) {
    if (portionId == null || authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    User principal = (User) authentication.getPrincipal();
    return portionRepository.existsByIdAndUserId(portionId, principal.getId());
  }

  public boolean isMealOwner(Long mealId, Authentication authentication) {
    if (mealId == null || authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    User principal = (User) authentication.getPrincipal();
    return mealRepository.existsByIdAndUserId(mealId, principal.getId());
  }
}
