package com.lirium.nutrition.dto.request;

public record RestrictionCreateRequestDTO(
        String code,
        String name,
        String category,
        String description
) {}
