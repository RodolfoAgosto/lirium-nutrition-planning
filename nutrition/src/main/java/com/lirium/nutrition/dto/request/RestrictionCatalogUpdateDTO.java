package com.lirium.nutrition.dto.request;

public record RestrictionCatalogUpdateDTO(
        String code,
        String name,
        String category,
        String description
){}