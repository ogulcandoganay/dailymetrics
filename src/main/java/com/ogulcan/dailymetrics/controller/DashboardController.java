package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.ActivityMetricDTO;
import com.ogulcan.dailymetrics.dto.ActivityTypeDTO;
import com.ogulcan.dailymetrics.dto.MetricDTO;
import com.ogulcan.dailymetrics.model.Metric;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.service.MetricService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final MetricService metricService;

    public DashboardController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<ActivityMetricDTO>> getDashboard(@AuthenticationPrincipal User user) {
        List<ActivityMetricDTO> dashboardData = metricService.getDashboardData(user)
                .stream()
                .map(summary -> new ActivityMetricDTO(
                        new ActivityTypeDTO(
                                summary.activity().getId(),
                                summary.activity().getName(),
                                summary.activity().getImage()
                        ),
                        summary.todayCount(),
                        summary.yesterdayCount(),
                        summary.totalAll(),
                        summary.totalMonth(),
                        summary.totalYear(),
                        summary.streak()
                ))
                .toList();
        return ResponseEntity.ok(dashboardData);
    }

    @PostMapping("/metrics/increment")
    public ResponseEntity<MetricDTO> incrementMetric(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody IncrementRequest request) {
        Metric metric = metricService.incrementMetric(user, request.getActivityTypeId(), request.getIncrementValue());
        MetricDTO metricDTO = new MetricDTO(
                metric.getId(),
                metric.getActivityType().getId(),
                metric.getCount(),
                metric.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        return ResponseEntity.ok(metricDTO);
    }
}

class IncrementRequest {
    private Long activityTypeId;
    private int incrementValue;

    public Long getActivityTypeId() {
        return activityTypeId;
    }

    public void setActivityTypeId(Long activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public int getIncrementValue() {
        return incrementValue;
    }

    public void setIncrementValue(int incrementValue) {
        this.incrementValue = incrementValue;
    }
}