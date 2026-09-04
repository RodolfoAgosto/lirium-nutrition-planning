package com.lirium.nutrition.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuth2ExchangeRequestDTO(
    @NotBlank(message = "OAuth2 authorization code is required") String code) {}
