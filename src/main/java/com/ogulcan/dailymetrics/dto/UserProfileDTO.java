package com.ogulcan.dailymetrics.dto;

public record UserProfileDTO(
        Long id,
        String username,
        String profilePhoto,
        String createdAt,
        String loginCode,
        boolean isAdmin
) {
}
