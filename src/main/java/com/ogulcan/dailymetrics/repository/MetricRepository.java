package com.ogulcan.dailymetrics.repository;

import com.ogulcan.dailymetrics.model.Metric;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    Optional<Metric> findByUserAndActivityTypeAndDate(User user, ActivityType activityType, LocalDate date);
    List<Metric> findAllByUserAndActivityType(User user, ActivityType activityType);
}