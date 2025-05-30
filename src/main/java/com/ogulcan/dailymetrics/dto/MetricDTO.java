package com.ogulcan.dailymetrics.dto;

public record MetricDTO(Long id, Long activityTypeId, int count, String date) {
}