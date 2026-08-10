package com.lirium.nutrition.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for refreshing the JWT access token")
public record RefreshRequestDTO(

        @Schema(
                description = "Valid refresh token issued during login or previous refresh",
                example = "d9b2a1e4-8f2c-4b5a-9d10-3c1234567890"
        )
        @NotBlank(message = "Refresh token is mandatory")
        String refreshToken

) { }