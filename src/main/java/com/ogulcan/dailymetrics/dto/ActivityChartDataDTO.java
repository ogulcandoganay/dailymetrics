package com.ogulcan.dailymetrics.dto;

import java.util.List;

public record ActivityChartDataDTO(
        Long activityId,
        String activityName,
        String chartType, // "daily", "weekly", "monthly"
        List<ChartDataItemDTO> data,
        ChartStatsDTO stats
) {
}
