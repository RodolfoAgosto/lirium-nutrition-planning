package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;

public interface NutritionPlanAssembler {

    NutritionPlan assemble(PatientProfile patient, Calories calories, MacroDistribution macros);

}
