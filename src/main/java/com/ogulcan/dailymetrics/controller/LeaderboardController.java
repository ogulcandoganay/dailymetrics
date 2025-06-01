package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.LeaderboardResponseDTO;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
// Frontend'in çalıştığı port için CORS ayarı (Diğer controller'larındaki gibi)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard(
            @RequestParam(name = "activityId", required = false) Long activityId,
            @RequestParam(name = "period", defaultValue = "all") String period,
            @AuthenticationPrincipal User currentUser){

        // currentUser null olabilir eğer endpoint @PreAuthorize gibi bir anotasyonla korunmuyorsa
        // ve public erişime açıksa. Ancak SecurityConfig'de .anyRequest().authenticated() olduğu için
        // buraya gelen istekte currentUser dolu olmalı. Yine de null check yapmak isteyebilirsin.
        if (currentUser == null) {
            // Bu durumun normalde SecurityConfig tarafından engellenmesi gerekir.
            // Eğer buraya düşerse, bir yetkilendirme sorunu olabilir.
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        LeaderboardResponseDTO responseDTO = leaderboardService.getLeaderboard(activityId, period);
        return ResponseEntity.ok(responseDTO);
    }

}
