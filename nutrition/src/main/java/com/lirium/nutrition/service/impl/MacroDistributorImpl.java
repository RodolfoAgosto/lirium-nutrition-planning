package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.service.MacroDistributor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MacroDistributorImpl implements MacroDistributor {

    @Override
    public MacroDistribution distribute(PatientProfile patient, Calories calories) {

        var activityLevel = patient.getActivityLevel();
        var sex = patient.getSex();

        double weightKg = patient.getWeight().toKg();
        double dailyCalories = calories.amount();

        log.info("Distributing macros for patientId={} calories={}", patient.getUser().getId(), dailyCalories);

        if (log.isDebugEnabled()) { log.debug("Input data weightKg={} sex={} activityLevel={}", weightKg, sex, activityLevel); }

        // proteinGrams → grams per kg according to Sex + Activity Level × weight
        double proteinFactor = activityLevel.proteinFactor(sex);
        double proteinGrams = weightKg * proteinFactor;

        if (log.isDebugEnabled()) { log.debug("Protein calculation factor={} grams={}", proteinFactor, proteinGrams); }

        // fatGrams → percentage based on Sex + ActivityLevel × dailyCalories
        double fatPercentage = activityLevel.fatPercentage(sex);
        double fatCalories = dailyCalories * fatPercentage;
        double fatGrams = fatCalories / 9;

        if (log.isDebugEnabled()) { log.debug("Fat calculation percentage={} grams={}", fatPercentage, fatGrams); }

        // carbGrams → dailyCalories - (protein*4 + fat*9) / 4
        double remainingCalories = dailyCalories - (proteinGrams * 4 + fatGrams * 9);
        double carbGrams = remainingCalories / 4;

        if (log.isDebugEnabled()) { log.debug("Carbs calculation remainingCalories={} grams={}", remainingCalories, carbGrams); }

        int protein = (int) Math.round(proteinGrams);
        int carbs = (int) Math.round(carbGrams);
        int fat = (int) Math.round(fatGrams);

        log.info("Macros distributed successfully patientId={} protein={} carbs={} fat={}", patient.getUser().getId(), protein, carbs, fat);

        return new MacroDistribution(protein, carbs, fat);

    }

    @Override
    public MacroDistribution distributeFromTemplate(Calories calories, NutritionPlanTemplate template) {
        int totalCal = calories.amount();

        log.info("Distributing macros from template templateId={} calories={}", template.getId(), totalCal);

        if (log.isDebugEnabled()) { log.debug("Template percentages protein={} carb={} fat={}", template.getProteinPercentage(), template.getCarbPercentage(), template.getFatPercentage()); }

        int proteinGrams = (int)((totalCal * template.getProteinPercentage() / 100.0) / 4);
        int carbGrams    = (int)((totalCal * template.getCarbPercentage()    / 100.0) / 4);
        int fatGrams     = (int)((totalCal * template.getFatPercentage()     / 100.0) / 9);

        log.info("Macros distributed from template templateId={} protein={} carbs={} fat={}", template.getId(), proteinGrams, carbGrams, fatGrams);

        return new MacroDistribution(proteinGrams, carbGrams, fatGrams);

    }

}
