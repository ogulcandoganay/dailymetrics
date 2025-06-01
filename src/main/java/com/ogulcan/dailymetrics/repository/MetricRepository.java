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
}
