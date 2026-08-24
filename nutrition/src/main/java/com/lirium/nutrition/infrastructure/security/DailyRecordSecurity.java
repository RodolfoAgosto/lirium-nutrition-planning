package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("dailyRecordSecurity")
@RequiredArgsConstructor
public class DailyRecordSecurity {

    private final DailyRecordRepository dailyRecordRepository;

    @Transactional(readOnly = true)
    public boolean isMealRecordOwner(Long mealRecordId, Authentication authentication) {
        if (mealRecordId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        User principal = (User) authentication.getPrincipal();

        return dailyRecordRepository.existsByMeals_IdAndPatient_User_Id(mealRecordId, principal.getId());
    }
}