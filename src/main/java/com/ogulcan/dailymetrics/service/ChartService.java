package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.ActivityChartDataDTO;
import com.ogulcan.dailymetrics.dto.ChartDataItemDTO;
import com.ogulcan.dailymetrics.dto.ChartStatsDTO;
import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.model.Metric;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import com.ogulcan.dailymetrics.repository.MetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private final MetricRepository metricRepository;
    private final ActivityTypeRepository activityTypeRepository;

    // Farklı tarih formatları için DateTimeFormatter'lar
    private static final DateTimeFormatter DAILY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private static final DateTimeFormatter MONTHLY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter RAW_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    public ChartService(MetricRepository metricRepository, ActivityTypeRepository activityTypeRepository) {
        this.metricRepository = metricRepository;
        this.activityTypeRepository = activityTypeRepository;
    }

    @Transactional(readOnly = true)
    public ActivityChartDataDTO getActivityChartData(User currentUser, Long activityId, String chartType) {
        ActivityType activityType = activityTypeRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + activityId));

        List<ChartDataItemDTO> dataItems;
        LocalDate today = LocalDate.now();

        switch (chartType.toLowerCase()) {
            case "daily":
                dataItems = getDailyData(currentUser.getId(), activityId, today);
                break;
            case "weekly":
                dataItems = getWeeklyData(currentUser.getId(), activityId, today);
                break;
            case "monthly":
                dataItems = getMonthlyData(currentUser.getId(), activityId, today);
                break;
            default:
                throw new IllegalArgumentException("Invalid chart type: " + chartType);
        }

        ChartStatsDTO stats = calculateStats(dataItems);

        return new ActivityChartDataDTO(
                activityType.getId(),
                activityType.getName(),
                chartType.toLowerCase(),
                dataItems,
                stats
        );
    }

    private List<ChartDataItemDTO> getDailyData(Long userId, Long activityId, LocalDate today) {
        LocalDate startDate = today.minusDays(29); // Son 30 gün (bugün dahil)
        LocalDate endDate = today;

        List<Metric> metrics = metricRepository.findAllByUser_IdAndActivityType_IdAndDateBetweenOrderByDateAsc(
                userId, activityId, startDate, endDate);

        Map<LocalDate, Integer> countsByDate = metrics.stream()
                .collect(Collectors.groupingBy(Metric::getDate, Collectors.summingInt(Metric::getCount)));

        List<ChartDataItemDTO> dailyData = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            int count = countsByDate.getOrDefault(currentDate, 0);
            dailyData.add(new ChartDataItemDTO(
                    currentDate.format(DAILY_LABEL_FORMATTER), // Örn: "May 26"
                    currentDate.format(RAW_DATE_FORMATTER),    // Örn: "2025-05-26"
                    count
            ));
        }
        return dailyData;
    }

    private List<ChartDataItemDTO> getWeeklyData(Long userId, Long activityId, LocalDate today) {
        // Haftanın ilk gününü Pazartesi olarak alalım (Locale.getDefault() yerine spesifik bir Locale daha iyi olabilir)
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        LocalDate endOfWeekForToday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)); // Veya haftanın sonu nasıl tanımlanıyorsa
        LocalDate startDate = endOfWeekForToday.minusWeeks(11).with(TemporalAdjusters.previousOrSame(firstDayOfWeek)); // Son 12 hafta
        LocalDate endDate = endOfWeekForToday;


        List<Metric> metrics = metricRepository.findAllByUser_IdAndActivityType_IdAndDateBetweenOrderByDateAsc(
                userId, activityId, startDate, endDate);

        Map<LocalDate, Integer> countsByWeekStart = metrics.stream()
                .collect(Collectors.groupingBy(
                        metric -> metric.getDate().with(TemporalAdjusters.previousOrSame(firstDayOfWeek)),
                        Collectors.summingInt(Metric::getCount)
                ));

        List<ChartDataItemDTO> weeklyData = new ArrayList<>();
        LocalDate currentWeekStartDate = startDate;
        for (int i = 0; i < 12; i++) { // Son 12 hafta
            int count = countsByWeekStart.getOrDefault(currentWeekStartDate, 0);
            // Haftalık etiket için örnek: "W22 (May 26)" veya sadece haftanın başlangıç tarihi
            String weekLabel = "W" + currentWeekStartDate.format(DateTimeFormatter.ofPattern("ww")) +
                    " (" + currentWeekStartDate.format(DAILY_LABEL_FORMATTER) + ")";

            weeklyData.add(new ChartDataItemDTO(
                    weekLabel,
                    currentWeekStartDate.format(RAW_DATE_FORMATTER),
                    count
            ));
            currentWeekStartDate = currentWeekStartDate.plusWeeks(1);
        }
        return weeklyData;
    }

    private List<ChartDataItemDTO> getMonthlyData(Long userId, Long activityId, LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth startMonth = currentMonth.minusMonths(11); // Son 12 ay
        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Metric> metrics = metricRepository.findAllByUser_IdAndActivityType_IdAndDateBetweenOrderByDateAsc(
                userId, activityId, startDate, endDate);

        Map<YearMonth, Integer> countsByMonth = metrics.stream()
                .collect(Collectors.groupingBy(
                        metric -> YearMonth.from(metric.getDate()),
                        Collectors.summingInt(Metric::getCount)
                ));

        List<ChartDataItemDTO> monthlyData = new ArrayList<>();
        YearMonth iterMonth = startMonth;
        for (int i = 0; i < 12; i++) { // Son 12 ay
            int count = countsByMonth.getOrDefault(iterMonth, 0);
            monthlyData.add(new ChartDataItemDTO(
                    iterMonth.format(MONTHLY_LABEL_FORMATTER), // Örn: "May 2025"
                    iterMonth.atDay(1).format(RAW_DATE_FORMATTER), // Ayın ilk günü
                    count
            ));
            iterMonth = iterMonth.plusMonths(1);
        }
        return monthlyData;
    }

    private ChartStatsDTO calculateStats(List<ChartDataItemDTO> dataItems) {
        if (dataItems == null || dataItems.isEmpty()) {
            return new ChartStatsDTO(0, 0, 0);
        }
        long total = dataItems.stream().mapToLong(ChartDataItemDTO::count).sum();
        int max = dataItems.stream().mapToInt(ChartDataItemDTO::count).max().orElse(0);
        double average = (double) total / dataItems.size();

        // Ortalamayı virgülden sonra bir basamak olacak şekilde yuvarla
        average = Math.round(average * 10.0) / 10.0;

        return new ChartStatsDTO(average, max, total);
    }
}