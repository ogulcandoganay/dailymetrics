package com.ogulcan.dailymetrics.repository;

import com.ogulcan.dailymetrics.model.Metric;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    Optional<Metric> findByUserAndActivityTypeAndDate(User user, ActivityType activityType, LocalDate date);
    List<Metric> findAllByUserAndActivityType(User user, ActivityType activityType);

    @Query("SELECT u.id, u.username, u.profilePhoto, SUM(m.count) as totalScore " +
            "FROM Metric m JOIN m.user u " + // User'a açıkça join yap ve 'u' alias'ını ver
            "WHERE m.activityType.id = :activityTypeId " +
            "AND (:startDate IS NULL OR m.date >= :startDate) " +
            "GROUP BY u.id, u.username, u.profilePhoto " + // Gruplamayı 'u' alias'ı üzerinden yap
            "ORDER BY SUM(m.count) DESC") // Alias yerine aggregate fonksiyonu ile sırala
    List<Object[]> findLeaderboardDataByActivityAndDate(
            @Param("activityTypeId") Long activityTypeId,
            @Param("startDate") LocalDate startDate
    );

    // NATIVE SQL QUERY İLE GÜNCELLENMİŞ METOT
    @Query(value = "SELECT " +
            "    m.activity_type_id as activityTypeId, " +        // activity_type tablosundan alınacak
            "    at.name as activityName, " +                     // activity_type tablosundan alınacak
            "    at.image as activityImage, " +                   // activity_type tablosundan alınacak
            "    m.count as maxCount, " +
            "    MIN(m.date) as firstAchievedDate " +
            "FROM " +
            "    metric m " + // Tablo adı: metric (entity adı değil)
            "JOIN " +
            "    activity_type at ON m.activity_type_id = at.id " + // activity_type tablosuna join
            "WHERE " +
            "    m.user_id = :userId AND " +
            "    m.count = (SELECT MAX(m2.count) " +
            "               FROM metric m2 " + // Tablo adı: metric
            "               WHERE m2.user_id = :userId AND m2.activity_type_id = m.activity_type_id) " +
            "GROUP BY " +
            "    m.activity_type_id, at.name, at.image, m.count " +
            "ORDER BY " +
            "    at.name ASC", nativeQuery = true) // nativeQuery = true eklendi
    List<Object[]> findPersonalBestRecordsByUserId(@Param("userId") Long userId);

    List<Metric> findAllByUser_IdAndActivityType_IdAndDateBetweenOrderByDateAsc(
            Long userId,
            Long activityTypeId,
            LocalDate startDate,
            LocalDate endDate
    );
}
