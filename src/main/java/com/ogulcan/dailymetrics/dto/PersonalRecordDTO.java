package com.ogulcan.dailymetrics.dto;

import java.time.LocalDate;

public record PersonalRecordDTO(
        Long activityId,
        String activityName,
        String activityImage, // Tam URL olacak (örn: http://localhost:8080/uploads/activities/...)
        int maxCount,
        LocalDate dateAchieved
) {
}
