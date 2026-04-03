package com.lirium.nutrition.model.valueobject;

public record MacroDistribution(
        int proteinGrams,
        int carbGrams,
        int fatGrams
) {}