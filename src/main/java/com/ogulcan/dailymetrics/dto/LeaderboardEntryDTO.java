package com.ogulcan.dailymetrics.dto;

public record LeaderboardEntryDTO(
        int rank,
        Long userId,
        String username,
        String profilePhoto,
        long totalScore
) {
}
