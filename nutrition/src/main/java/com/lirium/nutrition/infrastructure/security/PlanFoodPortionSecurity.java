package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.repository.PlanFoodPortionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("planFoodPortionSecurity")
@RequiredArgsConstructor
public class PlanFoodPortionSecurity {

    private final PlanFoodPortionRepository portionRepository;

    @Transactional(readOnly = true)
    public boolean isPortionOwner(Long portionId, Authentication authentication) {
        if (portionId == null || authentication == null) {
            return false;
        }

        String userEmail = authentication.getName();

        return portionRepository.findById(portionId)
                .map(portion -> portion.getMeal()
                        .getDailyPlan()
                        .getNutritionPlan()
                        .getPatientProfile()
                        .getUser()
                        .getEmail()
                        .equals(userEmail))
                .orElse(false);
    }
}