package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Column;

public record MacroDistribution(
    @Column(name = "protein_grams") int proteinGrams,
    @Column(name = "carb_grams") int carbGrams,
    @Column(name = "fat_grams") int fatGrams) {}
