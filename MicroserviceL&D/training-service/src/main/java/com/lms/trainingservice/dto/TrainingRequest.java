package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Training Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    private String difficultyLevel;

    private BigDecimal durationHours;

    private String thumbnailUrl;

    private String status;

}
