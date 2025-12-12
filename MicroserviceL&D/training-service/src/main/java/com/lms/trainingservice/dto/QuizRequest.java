package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Quiz Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    @NotNull(message = "Module ID is required")
    private UUID moduleId;

    @NotBlank(message = "Quiz title is required")
    private String title;

    private Integer passingScore;

    private Integer timeLimitMinutes;

}
