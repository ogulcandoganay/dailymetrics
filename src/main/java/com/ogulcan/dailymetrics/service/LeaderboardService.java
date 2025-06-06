package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.ActivityTypeDTO;
import com.ogulcan.dailymetrics.dto.LeaderboardEntryDTO;
import com.ogulcan.dailymetrics.dto.LeaderboardResponseDTO;
import com.ogulcan.dailymetrics.dto.PersonalRecordDTO;
import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.MetricRepository;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final MetricRepository metricRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final String defaultProfilePhotoPath;
    private final String backendBaseUrl;

    public LeaderboardService(MetricRepository metricRepository,
                              ActivityTypeRepository activityTypeRepository,
                              @Value("${app.default.profile-photo:/images/default-profile.png}") String defaultProfilePhotoPath,
                              @Value("${app.backend.base-url:http://localhost:8080}") String backendBaseUrl) {
        this.metricRepository = metricRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.defaultProfilePhotoPath = defaultProfilePhotoPath;
        this.backendBaseUrl = backendBaseUrl;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponseDTO getLeaderboard(Long activityIdInput, String period, User currentUser) {
        List<ActivityType> allActivityEntities = activityTypeRepository.findAll();
        if (allActivityEntities.isEmpty()) {
            return new LeaderboardResponseDTO(new ArrayList<>(), null, period, new ArrayList<>(), new ArrayList<>());
        }

        List<ActivityTypeDTO> allActivitiesDTO = allActivityEntities.stream()
                .map(activity -> new ActivityTypeDTO(activity.getId(), activity.getName(), formatImageUrl(activity.getImage())))
                .collect(Collectors.toList());

        Long currentActivityId = activityIdInput;
        if (currentActivityId == null && !allActivityEntities.isEmpty()) {
            currentActivityId = allActivityEntities.get(0).getId();
        }

        ActivityType selectedActivityEntity = null;
        if (currentActivityId != null) {
            selectedActivityEntity = activityTypeRepository.findById(currentActivityId)
                    .orElse(!allActivityEntities.isEmpty() ? allActivityEntities.get(0) : null);
        } else if (!allActivityEntities.isEmpty()) {
            selectedActivityEntity = allActivityEntities.get(0);
        }

        ActivityTypeDTO selectedActivityDTO = new ActivityTypeDTO(
                selectedActivityEntity.getId(),
                selectedActivityEntity.getName(),
                formatImageUrl(selectedActivityEntity.getImage())
        );

        LocalDate startDate = null;
        LocalDate today = LocalDate.now();

        switch (period.toLowerCase()) {
            case "today": startDate = today; break;
            case "week": startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); break;
            case "month": startDate = today.withDayOfMonth(1); break;
            case "year": startDate = today.withDayOfYear(1); break;
            default: startDate = null; break; // "all" veya tanımsız periyotlar için
        }

        List<Object[]> rawLeaderboardData = metricRepository.findLeaderboardDataByActivityAndDate(
                selectedActivityEntity.getId(),
                startDate
        );

        List<LeaderboardEntryDTO> results = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rawLeaderboardData) {
            Long userId = (Long) row[0];
            String username = (String) row[1];
            String profilePhotoRelativePath = (String) row[2];
            Long totalScoreRaw = (Long) row[3];
            long totalScore = (totalScoreRaw != null) ? totalScoreRaw : 0L;

            String profilePhotoUrl = formatImageUrl(profilePhotoRelativePath);
            // Eğer formatImageUrl null dönerse (yani DB'de yol yoksa veya "null" string ise) varsayılanı ata
            if (profilePhotoUrl == null) {
                profilePhotoUrl = backendBaseUrl + defaultProfilePhotoPath;
            }

            results.add(new LeaderboardEntryDTO(rank++, userId, username, profilePhotoUrl, totalScore));
        }

        List<PersonalRecordDTO> personalRecords = new ArrayList<>();
        if (currentUser != null) {
            List<Object[]> rawPersonalRecords = metricRepository.findPersonalBestRecordsByUserId(currentUser.getId());
            for (Object[] row : rawPersonalRecords) {
                Long activityRecordId = (Long) row[0];
                String activityName = (String) row[1];
                String activityImageRelativePath = (String) row[2];
                Number maxCountRaw = (Number) row[3]; // Native query'den Number olarak gelebilir
                // java.sql.Date olarak gelen tarihi LocalDate'e çeviriyoruz
                Object dateObject = row[4]; // Önce Object olarak al
                LocalDate dateAchieved = null;
                if (dateObject instanceof java.sql.Date) {
                    dateAchieved = ((java.sql.Date) dateObject).toLocalDate();
                } else if (dateObject instanceof java.time.LocalDate) { // Bazı JDBC sürücüleri doğrudan LocalDate dönebilir
                    dateAchieved = (java.time.LocalDate) dateObject;
                }
                // Gerekirse Timestamp için de kontrol eklenebilir: else if (dateObject instanceof java.sql.Timestamp) { dateAchieved = ((java.sql.Timestamp) dateObject).toLocalDateTime().toLocalDate(); }


                int maxCount = (maxCountRaw != null) ? maxCountRaw.intValue() : 0;
                String activityImageUrl = formatImageUrl(activityImageRelativePath);

                personalRecords.add(new PersonalRecordDTO(
                        activityRecordId,
                        activityName,
                        activityImageUrl,
                        maxCount,
                        dateAchieved
                ));
            }
        }

        return new LeaderboardResponseDTO(results, selectedActivityDTO, period, allActivitiesDTO, personalRecords);
    }

    private String formatImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.equalsIgnoreCase("null")) {
            return null;
        }
        // Zaten tam URL ise doğrudan döndür
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        // Göreceli yolun başında '/' olduğundan emin ol, yoksa ekle
        String correctedPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        return backendBaseUrl + correctedPath;
    }
}