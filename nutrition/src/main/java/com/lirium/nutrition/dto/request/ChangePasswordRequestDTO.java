package com.lirium.nutrition.dto.request;

public record ChangePasswordRequestDTO(
        String oldPassword,
        String newPassword
) {}