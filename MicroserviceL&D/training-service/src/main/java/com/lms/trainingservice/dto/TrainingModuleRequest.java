package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Training Module Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModuleRequest {

    @NotNull(message = "Training ID is required")
    private UUID trainingId;

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;

    private Integer sequenceOrder;

    private String contentType;

    private Integer estimatedDurationMinutes;

}
