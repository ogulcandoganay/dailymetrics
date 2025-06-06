package com.ogulcan.dailymetrics.dto;

public record ChartDataItemDTO(
        String dateLabel, // Grafikte gösterilecek etiket (örn: "May 26", "W22", "May '25")
        String rawDate,   // Asıl tarih (örn: "2025-05-26")
        int count         // O periyottaki değer
) {
}