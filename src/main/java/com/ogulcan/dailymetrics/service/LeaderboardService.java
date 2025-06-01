package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.ActivityTypeDTO;
import com.ogulcan.dailymetrics.dto.LeaderboardEntryDTO;
import com.ogulcan.dailymetrics.dto.LeaderboardResponseDTO;
import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.repository.MetricRepository;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import org.springframework.beans.factory.annotation.Value; // BACKEND_URL için
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Okuma işlemi için

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final MetricRepository metricRepository;
    private final ActivityTypeRepository activityTypeRepository; // ActivityTypeService yerine direkt repo da olur
    private final String defaultProfilePhotoPath; // Varsayılan profil fotoğrafı yolu
    private final String backendBaseUrl; // Resim yolları için

    public LeaderboardService(MetricRepository metricRepository,
                              ActivityTypeRepository activityTypeRepository,
                              @Value("${app.default.profile-photo:/images/default-profile.png}") String defaultProfilePhotoPath,
                              @Value("${app.backend.base-url:http://localhost:8080}") String backendBaseUrl) {
        this.metricRepository = metricRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.defaultProfilePhotoPath = defaultProfilePhotoPath;
        this.backendBaseUrl = backendBaseUrl;
    }

    @Transactional
    public LeaderboardResponseDTO getLeaderboard(Long activityIdInput, String period) {
        // 1. Tüm aktiviteleri çek
        List<ActivityType> allActivityEntities = activityTypeRepository.findAll();
        if (allActivityEntities.isEmpty()) {
            // Hiç aktivite yoksa boş bir response dön
            return new LeaderboardResponseDTO(new ArrayList<>(), null, period, new ArrayList<>());
        }
        List<ActivityTypeDTO> allActivitiesDTO = allActivityEntities.stream()
                .map(activity -> new ActivityTypeDTO(activity.getId(), activity.getName(),
                        formatImageUrl(activity.getImage())))
                .collect(Collectors.toList());

        // 2. activityId null ise veya geçerli değilse varsayılanı ata
        Long currentActivityId = activityIdInput;
        if (currentActivityId == null) {
            currentActivityId = allActivityEntities.get(0).getId();
        }

        // 3. Seçili aktiviteyi bul ve DTO'ya dönüştür
        ActivityType selectedActivityEntity = activityTypeRepository.findById(currentActivityId)
                .orElse(allActivityEntities.get(0)); // Bulamazsa ilkini varsay

        ActivityTypeDTO selectedActivityDTO = new ActivityTypeDTO(
                selectedActivityEntity.getId(),
                selectedActivityEntity.getName(),
                formatImageUrl(selectedActivityEntity.getImage())
        );

        // 4. Periyoda göre başlangıç tarihini belirle
        LocalDate startDate = null;
        LocalDate today = LocalDate.now(); // Sistemin lokal tarihini kullanır, UTC için Clock enjekte edilebilir

        switch (period.toLowerCase()) {
            case "today":
                startDate = today;
                break;
            case "week":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "month":
                startDate = today.withDayOfMonth(1);
                break;
            case "year":
                startDate = today.withDayOfYear(1);
                break;
            case "all":
            default:
                startDate = null; // Tüm zamanlar için null kalacak
                break;
        }

        // 5. Repository'den leaderboard verisini çek
        List<Object[]> rawLeaderboardData = metricRepository.findLeaderboardDataByActivityAndDate(
                selectedActivityEntity.getId(),
                startDate
        );

        // 6. Ham veriyi LeaderboardEntryDTO listesine dönüştür
        List<LeaderboardEntryDTO> results = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rawLeaderboardData) {
            Long userId = (Long) row[0];
            String username = (String) row[1];
            String profilePhotoRelativePath = (String) row[2];
            Long totalScoreRaw = (Long) row[3]; // SUM() Long döner

            long totalScore = (totalScoreRaw != null) ? totalScoreRaw : 0L;

            String profilePhotoUrl = formatImageUrl(profilePhotoRelativePath);
            if (profilePhotoUrl == null || profilePhotoUrl.equals(backendBaseUrl + "/null")) { // DB'den null gelebilir
                profilePhotoUrl = backendBaseUrl + defaultProfilePhotoPath; // Varsayılanı ata
            }

            results.add(new LeaderboardEntryDTO(
                    rank++,
                    userId,
                    username,
                    profilePhotoUrl, // Frontend'in direkt kullanabileceği tam URL
                    totalScore
            ));
        }

        // 7. LeaderboardResponseDTO'yu oluştur ve dön
        return new LeaderboardResponseDTO(results, selectedActivityDTO, period, allActivitiesDTO);
    }

    private String formatImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null; // Veya varsayılan bir resim yolu dönebilirsin
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        // Göreceli yolun başında '/' olduğundan emin ol, yoksa ekle
        String correctedPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        return backendBaseUrl + correctedPath;
    }
}
