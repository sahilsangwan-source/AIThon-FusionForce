package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Training Module Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModuleResponse {

    private UUID id;
    private UUID trainingId;
    private String title;
    private String description;
    private Integer sequenceOrder;
    private String contentType;
    private Integer estimatedDurationMinutes;
    private List<TrainingContentResponse> contents;
    private List<QuizResponse> quizzes;

}
