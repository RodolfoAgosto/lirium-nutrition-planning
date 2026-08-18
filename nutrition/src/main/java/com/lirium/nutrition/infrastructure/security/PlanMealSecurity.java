package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.repository.PlanMealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.lirium.nutrition.model.entity.User;

@Component("planMealSecurity")
@RequiredArgsConstructor
public class PlanMealSecurity {

    private final PlanMealRepository planMealRepository;

    public boolean isOwner(Long mealId, Authentication authentication) {
        if (mealId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        User principal = (User) authentication.getPrincipal();
        return planMealRepository.existsByIdAndUserId(mealId, principal.getId());
    }
}