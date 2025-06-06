package com.ogulcan.dailymetrics.dto;

public record ChartStatsDTO(
        double average,
        int max,
        long total // Toplamlar büyük olabilir
) {
}