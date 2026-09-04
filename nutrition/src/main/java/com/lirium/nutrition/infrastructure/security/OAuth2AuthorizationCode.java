package com.lirium.nutrition.infrastructure.security;

import java.time.Instant;

public record OAuth2AuthorizationCode(Long userId, Instant expiresAt) {}
