package com.lirium.nutrition.dto.request;

public record RestrictionUpdateDTO(
        String name,
        String category,
        String description
) {}
