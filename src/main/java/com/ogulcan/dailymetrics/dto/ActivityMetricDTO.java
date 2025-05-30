package com.ogulcan.dailymetrics.dto;

public record ActivityMetricDTO(
        ActivityTypeDTO activity,
        int todayCount,
        int yesterdayCount,
        int totalAll,
        int totalMonth,
        int totalYear,
        int streak
) {
}