package com.ogulcan.dailymetrics.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivityTypeDTO(
        Long id,
        @NotBlank(message = "Activity name cannot be empty")
        String name,
        String image
) {
}