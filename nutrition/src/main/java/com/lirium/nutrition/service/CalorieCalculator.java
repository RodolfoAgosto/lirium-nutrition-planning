package com.lirium.nutrition.service;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.valueobject.Calories;

public interface CalorieCalculator {

    Calories calculate(PatientProfile patient);

}
