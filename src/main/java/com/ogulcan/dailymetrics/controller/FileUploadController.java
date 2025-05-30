// FileUploadController.java - Profil fotoğrafı yüklemek için
package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.model.User;
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
public class FileUploadController {

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadProfilePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam("photo") MultipartFile photo) {

        // Sadece kendi fotoğrafını yükleyebilir
        if (!user.getId().equals(id)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        if (photo.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        try {
            // Klasör oluştur
            Path profilesDir = Paths.get(uploadDir, "profiles");
            Files.createDirectories(profilesDir);

            // Unique dosya adı
            String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path filePath = profilesDir.resolve(filename);

            // Dosyayı kaydet
            Files.write(filePath, photo.getBytes());

            // Eski fotoğrafı sil (varsa)
            if (user.getProfilePhoto() != null) {
                try {
                    Path oldFile = Paths.get("src/main/resources/static", user.getProfilePhoto());
                    Files.deleteIfExists(oldFile);
                } catch (Exception e) {
                    // Ignore deletion errors
                }
            }

            // Kullanıcıyı güncelle
            String photoPath = "/uploads/profiles/" + filename;
            user.setProfilePhoto(photoPath);
            // UserService'i enjekte edip user'ı kaydetmek gerekir

            return ResponseEntity.ok(photoPath);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
