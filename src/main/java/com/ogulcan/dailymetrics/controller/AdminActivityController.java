package com.ogulcan.dailymetrics.controller;

import com.ogulcan.dailymetrics.dto.ActivityTypeDTO;
import com.ogulcan.dailymetrics.service.ActivityTypeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final ActivityTypeService activityTypeService;

    public AdminActivityController(ActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ActivityTypeDTO> createActivity(
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(activityTypeService.createActivity(name, image));
    }

    @RolesAllowed("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityTypeService.deleteActivity(id);
        return ResponseEntity.ok().build();
    }

    @RolesAllowed("ROLE_ADMIN")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ActivityTypeDTO> updateActivity(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(activityTypeService.updateActivity(id, name, image));
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<List<ActivityTypeDTO>> getAllActivities() {
        return ResponseEntity.ok(activityTypeService.getAllActivities());
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeDTO> getActivityById(@PathVariable Long id) {
        ActivityTypeDTO activity = activityTypeService.getActivityById(id);
        return ResponseEntity.ok(activity);
    }
}