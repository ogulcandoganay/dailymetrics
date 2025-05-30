package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            System.out.println("Login attempt with code: " + loginRequest.getLoginCode());
            
            // Login code ile kullanıcıyı bul
            Optional<User> userOpt = userRepository.findByLoginCode(loginRequest.getLoginCode());
            
            if (userOpt.isEmpty()) {
                System.out.println("User not found for login code: " + loginRequest.getLoginCode());
                return ResponseEntity.status(401).body("Invalid login code");
            }

            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername());
            
            // Manuel authentication oluştur
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
            );

            // SecurityContext'e kaydet
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Session'a kaydet
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            System.out.println("Login successful for user: " + user.getUsername());

            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getUsername()));
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
        }
    }

    // Debug için test endpoint'i
    @GetMapping("/test")
    public ResponseEntity<?> test(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok("Authentication is null");
        }
        return ResponseEntity.ok("User: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
    }
}

class LoginRequest {
    private String loginCode;

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }
}

class LoginResponse {
    private Long userId;
    private String username;

    public LoginResponse(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}