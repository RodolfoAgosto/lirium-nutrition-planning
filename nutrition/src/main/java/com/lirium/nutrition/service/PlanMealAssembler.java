package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;

public interface PlanMealAssembler {

    void assemble(DailyPlan dailyPlan, PatientProfile patient, Calories calories, MacroDistribution macros);

}
