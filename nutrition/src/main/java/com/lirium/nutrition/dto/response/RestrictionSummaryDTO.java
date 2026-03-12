package com.lirium.nutrition.dto.response;

import com.lirium.nutrition.model.enums.RestrictionCategory;

public record RestrictionSummaryDTO(
        Long id,
        String code,
        String name,
        RestrictionCategory category
) {}