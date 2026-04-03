package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.valueobject.*;

public interface PlanFoodPortionAssembler {

    void assemble(PlanMeal planMeal, PatientProfile patient, Calories calories, Fat fat, Carbs carbs, Protein protein);

}
