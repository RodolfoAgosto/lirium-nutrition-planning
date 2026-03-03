package com.lirium.nutrition.dto.response;

public record UserSummaryDTO(
        Long id,
        String email,
        String firstName,
        String lastName
) {}
