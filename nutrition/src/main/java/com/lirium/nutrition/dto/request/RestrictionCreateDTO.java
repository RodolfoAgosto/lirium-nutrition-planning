package com.lirium.nutrition.dto.request;

public record RestrictionCreateDTO(
        String code,
        String name,
        String category,
        String description
) {}
