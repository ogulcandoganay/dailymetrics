package com.ogulcan.dailymetrics.dto;

import com.ogulcan.dailymetrics.model.ActivityType;

import java.util.List;

public record LeaderboardResponseDTO(
        List<LeaderboardEntryDTO> results,
        ActivityTypeDTO selectedActivity,
        String period,
        List<ActivityTypeDTO> allActivities,
        List<PersonalRecordDTO> personalRecords
) {
}
