package com.ogulcan.dailymetrics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_type")
@Getter
@Setter
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 255)
    private String image;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "activityType",      // Metric entity'sindeki 'activityType' alanının adı. Bu, ilişkinin Metric tarafında yönetildiğini belirtir.
            cascade = CascadeType.ALL,      // ActivityType üzerindeki tüm işlemler (persist, merge, REMOVE) ilişkili Metric'lere de yansıtılır.
            orphanRemoval = true,           // Bir Metric, ActivityType'ın metrics listesinden çıkarılırsa (artık "yetim" kalırsa) veritabanından silinir
            fetch = FetchType.LAZY          // Performans için: Metric'ler sadece ihtiyaç duyulduğunda yüklenir.
    )
    private List<Metric> metrics = new ArrayList<>();
}