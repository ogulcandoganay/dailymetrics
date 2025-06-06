package com.ogulcan.dailymetrics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metric", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "activity_type_id", "date"})
})
@Getter
@Setter
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
