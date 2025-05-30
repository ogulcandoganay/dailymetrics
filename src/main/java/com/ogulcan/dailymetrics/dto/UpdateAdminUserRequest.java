package com.ogulcan.dailymetrics.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAdminUserRequest(
        @NotBlank(message = "Username cannot be empty")
        String username,
        Boolean isAdmin,
        String profilePhoto
) {
}