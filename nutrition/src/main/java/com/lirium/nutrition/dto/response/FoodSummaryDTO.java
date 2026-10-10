package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.MeasureUnit;

public record FoodSummaryDTO(Long id, String name, MeasureUnit defaultUnit) {}
