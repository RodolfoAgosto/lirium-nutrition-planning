package com.lirium.nutrition.dto.response;

public record RestrictionResponseDTO(
        Long id,
        String code,
        String name,
        String category,
        String description
) {}