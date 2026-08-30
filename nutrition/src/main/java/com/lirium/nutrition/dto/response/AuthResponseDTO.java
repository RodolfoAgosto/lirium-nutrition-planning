package com.lirium.nutrition.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT tokens")
public record AuthResponseDTO(
    @Schema(
            description = "JWT access token used for authorizing subsequent API requests",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiw...")
        String token,
    @Schema(
            description = "Refresh token used to obtain a new access token when expired",
            example = "d9b2a1e4-8f2c-4b5a-9d10-3c1234567890")
        String refreshToken) {}
