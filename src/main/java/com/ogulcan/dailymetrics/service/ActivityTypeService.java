package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.dto.ActivityTypeDTO;
import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;
    private final String uploadDir;

    public ActivityTypeService(ActivityTypeRepository activityTypeRepository,
                               @Value("${app.upload.dir:src/main/resources/static/uploads/activities}") String uploadDir) {
        this.activityTypeRepository = activityTypeRepository;
        this.uploadDir = uploadDir;
        createUploadDir();
    }

    private void createUploadDir() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory", e);
        }
    }

    @Transactional
    public ActivityTypeDTO createActivity(String name, MultipartFile image) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Activity name cannot be empty");
        }
        if (activityTypeRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Activity name already exists");
        }

        ActivityType activityType = new ActivityType();
        activityType.setName(name);
        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            activityType.setImage(imagePath);
        }
        ActivityType savedActivity = activityTypeRepository.save(activityType);
        return new ActivityTypeDTO(savedActivity.getId(), savedActivity.getName(), savedActivity.getImage());
    }

    @Transactional
    public void deleteActivity(Long id) {
        ActivityType activity = activityTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        if (activity.getImage() != null) {
            deleteImage(activity.getImage());
        }
        activityTypeRepository.deleteById(id);
    }

    @Transactional
    public ActivityTypeDTO updateActivity(Long id, String name, MultipartFile image) {
        ActivityType activity = activityTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (name != null && !name.isBlank()) {
            if (!activity.getName().equals(name) && activityTypeRepository.findByName(name).isPresent()) {
                throw new IllegalArgumentException("Activity name already exists");
            }
            activity.setName(name);
        }
        if (image != null && !image.isEmpty()) {
            if (activity.getImage() != null) {
                deleteImage(activity.getImage());
            }
            String imagePath = saveImage(image);
            activity.setImage(imagePath);
        }
        ActivityType updatedActivity = activityTypeRepository.save(activity);
        return new ActivityTypeDTO(updatedActivity.getId(), updatedActivity.getName(), updatedActivity.getImage());
    }

    public List<ActivityTypeDTO> getAllActivities() {
        return activityTypeRepository.findAll()
                .stream()
                .map(activity -> new ActivityTypeDTO(activity.getId(), activity.getName(), activity.getImage()))
                .collect(Collectors.toList());
    }

    private String saveImage(MultipartFile image) {
        try {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, image.getBytes());
            return "/uploads/activities/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private void deleteImage(String imagePath) {
        try {
            Path filePath = Paths.get("src/main/resources/static" + imagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    public ActivityTypeDTO getActivityById(Long id) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        return new ActivityTypeDTO(activityType.getId(), activityType.getName(), activityType.getImage());
    }
}