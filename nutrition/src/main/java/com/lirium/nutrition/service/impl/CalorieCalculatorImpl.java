package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.service.CalorieCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class CalorieCalculatorImpl implements CalorieCalculator {

    @Override
    public Calories calculate(PatientProfile patient) {

        // Calculate calorie expenditure
        Calories calories;
        int age = Period.between(patient.getUser().getBirthDate(), LocalDate.now()).getYears();
        double weightKg = patient.getWeight().grams() / 1000.0;
        double heightCm = patient.getHeight().cm();
        int caloriesValue;

        if(patient.getSex() == Sex.MALE) {
            // MAN: (10 × Weight(kg) + (6.25 × Height(cm) − (5 × age) + 5
            caloriesValue = (int)((10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5);
        }else {
            // WOMAN:  (10 × kg) + (6.25 × cm) − (5 × age) − 161
            caloriesValue = (int)((10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161);
        }

        // Adjust by activity
        caloriesValue *= patient.getActivityLevel().getFactor();

        // Adjust by goal
        caloriesValue = (int)patient.getPrimaryGoal().adjust(caloriesValue);

        // Adjust by PhysiologicalCondition
        for (PhysiologicalCondition condition : patient.getPhysiologicalConditions()) {
            caloriesValue = (int)condition.adjust(caloriesValue);
        }

        return new Calories(caloriesValue);
    }
}
