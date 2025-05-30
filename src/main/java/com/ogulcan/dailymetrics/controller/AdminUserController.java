package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.CreateUserRequest;
import com.ogulcan.dailymetrics.dto.CreateUserResponse;
import com.ogulcan.dailymetrics.dto.UpdateAdminUserRequest;
import com.ogulcan.dailymetrics.dto.UserProfileDTO;
import com.ogulcan.dailymetrics.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @RolesAllowed("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @RolesAllowed("ROLE_ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateAdminUserRequest request) {
        userService.updateAdminUser(id, request);
        return ResponseEntity.ok().build();
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping("/{id}/reset-code")
    public ResponseEntity<String> resetLoginCode(@PathVariable Long id) {
        String newCode = userService.resetLoginCode(id);
        return ResponseEntity.ok(newCode);
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        List<UserProfileDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> findByUsernameContaining(@RequestParam("term") String searchTerm) {
        List<UserProfileDTO> users = userService.searchUsersByUsername(searchTerm);
        return ResponseEntity.ok(users);
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        UserProfileDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping("/{userId}/photo")
    public ResponseEntity<String> uploadUserProfilePhoto(@PathVariable Long userId, @RequestParam("photo") MultipartFile photo) {
        // TODO: UserService veya yeni bir FileStorageService kullanarak:
        // 1. Fotoğrafı kaydet (örn: src/main/resources/static/uploads/profiles/benzersiz_ad.jpg)
        //    - Benzersiz bir dosya adı oluştur.
        //    - Gerekirse eski fotoğrafı sil (opsiyonel, bu endpoint sadece yükleyip yol dönebilir).
        // 2. Kaydedilen dosyanın göreli yolunu oluştur (örn: "/uploads/profiles/benzersiz_ad.jpg").
        // 3. Bu göreli yolu String olarak döndür.
        //
        // Örnek (basitleştirilmiş, hata kontrolü ve servis katmanı eksik):
        if (photo.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }
        if (!photo.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }
        try {
            String uploadDir = "src/main/resources/static/uploads/profiles";
            Path profilesDirPath = Paths.get(uploadDir);
            Files.createDirectories(profilesDirPath);

            String filename = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
            Path filePath = profilesDirPath.resolve(filename);
            Files.write(filePath, photo.getBytes());

            String relativePath = "/uploads/profiles/" + filename;

            // Bu endpoint kullanıcının DB'deki photoPath'ini GÜNCELLEMEYEBİLİR.
            // Sadece dosyayı kaydeder ve yolunu döndürür.
            // Ana PUT /api/admin/users/{id} endpoint'i bu yolu alıp güncellemeyi yapar.
            return ResponseEntity.ok(relativePath);

        } catch (IOException e) {
            // Loglama ekleyin
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }

    }

}