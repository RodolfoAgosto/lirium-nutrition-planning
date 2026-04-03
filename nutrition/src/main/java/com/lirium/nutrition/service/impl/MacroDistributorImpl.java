package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.MacroDistributor;
import org.springframework.stereotype.Service;

@Service
public class MacroDistributorImpl implements MacroDistributor {

    @Override
    public MacroDistribution distribute(PatientProfile patient, Calories calories) {

        var activityLevel = patient.getActivityLevel();
        var sex = patient.getSex();

        double weightKg = patient.getWeight().toKg();
        double dailyCalories = calories.amount();

        // proteinGrams → grams per kg according to Sex + Activity Level × weight
        double proteinFactor = activityLevel.proteinFactor(sex);
        double proteinGrams = weightKg * proteinFactor;

        // fatGrams → percentage based on Sex + ActivityLevel × dailyCalories
        double fatPercentage = activityLevel.fatPercentage(sex);
        double fatCalories = dailyCalories * fatPercentage;
        double fatGrams = fatCalories / 9;

        // carbGrams → dailyCalories - (protein*4 + fat*9) / 4
        double remainingCalories =
                dailyCalories - (proteinGrams * 4 + fatGrams * 9);
        double carbGrams = remainingCalories / 4;

        return new MacroDistribution(
                (int) Math.round(proteinGrams),
                (int) Math.round(carbGrams),
                (int) Math.round(fatGrams)
        );
    }
}
