package com.ogulcan.dailymetrics.dto;

import com.ogulcan.dailymetrics.model.ActivityType;

public record ActivityMetricSummary(
        ActivityType activity,
        int todayCount,
        int yesterdayCount,
        int totalAll,
        int totalMonth,
        int totalYear,
        int streak
) {
}