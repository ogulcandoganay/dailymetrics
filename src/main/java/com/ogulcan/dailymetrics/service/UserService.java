package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.*;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public CreateUserResponse createUser(@Valid CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setAdmin(false);
        user.setLoginCode(generateLoginCode());
        User savedUser = userRepository.save(user);
        return new CreateUserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getLoginCode());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.isAdmin()) {
            throw new IllegalArgumentException("Cannot delete admin user");
        }
        userRepository.deleteById(id);
    }

        @Transactional
        public void updateAdminUser(Long id, UpdateAdminUserRequest request) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (request.username() != null && !request.username().isBlank()) {
                if (!user.getUsername().equals(request.username()) && userRepository.findByUsername(request.username()).isPresent()) {
                    throw new IllegalArgumentException("Username already exists");
                }
                user.setUsername(request.username());
            }
            if (request.isAdmin() != null) {
                user.setAdmin(request.isAdmin());
            }
            user.setProfilePhoto(request.profilePhoto());
            userRepository.save(user);
    }

    @Transactional
    public String resetLoginCode(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String newCode = generateLoginCode();
        user.setLoginCode(newCode);
        userRepository.save(user);
        return newCode;
    }

    public UserProfileDTO getCurrentUser(User user) {
        System.out.println("UserService.getCurrentUser called for: " + user.getUsername() + ", admin: " + user.isAdmin());
        
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getProfilePhoto(),
                user.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                user.getLoginCode(),
                user.isAdmin()
        );
    }

    @Transactional
    public void updateCurrentUser(User user, UpdateUserRequest request) {
        if (request.username() != null && !request.username().isBlank()) {
            if (!user.getUsername().equals(request.username()) && userRepository.findByUsername(request.username()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.username());
        }
        if (request.profilePhoto() != null) {
            user.setProfilePhoto(request.profilePhoto());
        }
        userRepository.save(user);
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserProfileDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getProfilePhoto(),
                        user.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                        user.getLoginCode(),
                        user.isAdmin()
                ))
                .collect(Collectors.toList());
    }

    // bu method'ta ismi iceren kelime ile arama yapılır, bu kayıda uyan tüm user'lar getirilir.
    public List<UserProfileDTO> searchUsersByUsername(String searchTerm){
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        return userRepository.findByUsernameContaining(searchTerm).stream()
                .map(user -> new UserProfileDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getProfilePhoto(),
                        user.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                        null,
                        user.isAdmin()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getProfilePhoto(),
                user.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                user.getLoginCode(), // loginCode'u null yap
                user.isAdmin()
        );
    }

    private String generateLoginCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}