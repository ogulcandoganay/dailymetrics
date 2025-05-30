package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.ActivityMetricSummary;
import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.model.Metric;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import com.ogulcan.dailymetrics.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final ActivityTypeRepository activityTypeRepository;

    public MetricService(MetricRepository metricRepository, ActivityTypeRepository activityTypeRepository) {
        this.metricRepository = metricRepository;
        this.activityTypeRepository = activityTypeRepository;
    }

    public Optional<Metric> findMetric(User user, ActivityType activityType, LocalDate date) {
        return metricRepository.findByUserAndActivityTypeAndDate(user, activityType, date);
    }

    public Metric incrementMetric(User user, Long activityTypeId, int incrementValue) {
        ActivityType activityType = activityTypeRepository.findById(activityTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Activity type not found: " + activityTypeId));

        LocalDate today = LocalDate.now();
        Optional<Metric> existingMetric = findMetric(user, activityType, today);

        Metric metric;
        if (existingMetric.isPresent()) {
            metric = existingMetric.get();
            metric.setCount(metric.getCount() + incrementValue);
        } else {
            metric = new Metric();
            metric.setUser(user);
            metric.setActivityType(activityType);
            metric.setCount(incrementValue);
            metric.setDate(today);
        }

        return metricRepository.save(metric);
    }

    public List<ActivityMetricSummary> getDashboardData(User user) {
        List<ActivityType> activities = activityTypeRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return activities.stream().map(activity -> {
            int todayCount = findMetric(user, activity, today)
                    .map(Metric::getCount)
                    .orElse(0);
            int yesterdayCount = findMetric(user, activity, yesterday)
                    .map(Metric::getCount)
                    .orElse(0);

            int totalAll = metricRepository.findAllByUserAndActivityType(user, activity)
                    .stream()
                    .mapToInt(Metric::getCount)
                    .sum();
            int totalMonth = metricRepository.findAllByUserAndActivityType(user, activity)
                    .stream()
                    .filter(m -> m.getDate().getYear() == today.getYear() && m.getDate().getMonth() == today.getMonth())
                    .mapToInt(Metric::getCount)
                    .sum();
            int totalYear = metricRepository.findAllByUserAndActivityType(user, activity)
                    .stream()
                    .filter(m -> m.getDate().getYear() == today.getYear())
                    .mapToInt(Metric::getCount)
                    .sum();

            int streak = calculateStreak(user, activity, today);

            return new ActivityMetricSummary(activity, todayCount, yesterdayCount, totalAll, totalMonth, totalYear, streak);
        }).toList();
    }

    private int calculateStreak(User user, ActivityType activity, LocalDate today) {
        int streak = 0;
        LocalDate date = today;

        while (true) {
            Optional<Metric> metric = findMetric(user, activity, date);
            if (metric.isEmpty() || metric.get().getCount() == 0) {
                break;
            }
            streak++;
            date = date.minusDays(1);
        }

        if (streak == 0 && findMetric(user, activity, today.minusDays(1)).map(Metric::getCount).orElse(0) > 0) {
            streak = 1;
        }

        return streak;
    }
}