package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.UpdateUserRequest;
import com.ogulcan.dailymetrics.dto.UserProfileDTO;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        System.out.println("Getting current user: " + user.getUsername() + ", isAdmin: " + user.isAdmin());
        UserProfileDTO userProfile = userService.getCurrentUser(user);
        System.out.println("Returning user profile: " + userProfile.username() + ", isAdmin: " + userProfile.isAdmin());
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateCurrentUser(user, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("photo") MultipartFile photo) {

        if (photo.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        if (!photo.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }

        try {
            // Klasör oluştur
            Path profilesDir = Paths.get(uploadDir, "profiles");
            Files.createDirectories(profilesDir);

            // Unique dosya adı
            String filename = UUID.randomUUID() + "_profile.jpg";
            Path filePath = profilesDir.resolve(filename);

            // Dosyayı kaydet
            Files.write(filePath, photo.getBytes());
            System.out.println("File saved to: " + filePath.toAbsolutePath());
            System.out.println("File exists: " + Files.exists(filePath));

            // Eski fotoğrafı sil (varsa)
            if (user.getProfilePhoto() != null) {
                try {
                    Path oldFile = Paths.get(uploadDir, user.getProfilePhoto().replace("/uploads/", ""));
                    Files.deleteIfExists(oldFile);
                    System.out.println("Old file deleted: " + oldFile.toAbsolutePath());
                } catch (Exception e) {
                    System.out.println("Failed to delete old file: " + e.getMessage());
                }
            }

            // Kullanıcıyı güncelle
            String photoPath = "/uploads/profiles/" + filename;
            UpdateUserRequest updateRequest = new UpdateUserRequest(user.getUsername(), photoPath);
            userService.updateCurrentUser(user, updateRequest);

            return ResponseEntity.ok(photoPath);
        } catch (IOException e) {
            System.out.println("Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}