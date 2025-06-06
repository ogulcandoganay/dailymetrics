package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.ActivityChartDataDTO;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.service.ChartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException; // Hata yönetimi için

@RestController
@RequestMapping("/api/charts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class ChartController {

    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<ActivityChartDataDTO> getActivityChartData(
            @PathVariable Long activityId,
            @RequestParam(name = "type", defaultValue = "daily") String chartType,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            // Normalde Spring Security bu durumu yakalar ve 401 döner,
            // ama ekstra bir güvence olarak eklenebilir.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        try {
            ActivityChartDataDTO chartData = chartService.getActivityChartData(currentUser, activityId, chartType);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            // Servis katmanında fırlatılan "Activity not found" veya "Invalid chart type" gibi hatalar için.
            // Daha spesifik exception sınıfları oluşturup ona göre farklı HTTP statüleri dönmek daha iyi olabilir.
            // Örneğin, aktivite bulunamazsa 404, geçersiz chartType için 400.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Beklenmedik diğer hatalar için genel bir 500 hatası
            // Gerçek bir uygulamada bu hatanın loglanması önemlidir.
            System.err.println("Error generating chart data: " + e.getMessage()); // Loglama örneği
            e.printStackTrace(); // Detaylı loglama
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating chart data");
        }
    }
}