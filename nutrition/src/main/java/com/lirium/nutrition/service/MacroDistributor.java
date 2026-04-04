package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;

public interface MacroDistributor {

    MacroDistribution distribute(PatientProfile patient, Calories calories);

    MacroDistribution distributeFromTemplate(Calories calories, NutritionPlanTemplate template);

}
