package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Training Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {

    private UUID id;
    private String title;
    private String description;
    private String category;
    private String difficultyLevel;
    private BigDecimal durationHours;
    private String thumbnailUrl;
    private UUID createdBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TrainingModuleResponse> modules;

}
