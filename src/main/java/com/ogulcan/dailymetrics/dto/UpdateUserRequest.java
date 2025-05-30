package com.ogulcan.dailymetrics.dto;

public record UpdateUserRequest(
        String username,
        String profilePhoto
) {
}
