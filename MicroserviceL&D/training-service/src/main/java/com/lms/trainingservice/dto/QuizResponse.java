package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Quiz Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    private UUID id;
    private UUID moduleId;
    private String title;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private List<QuizQuestionResponse> questions;

}
