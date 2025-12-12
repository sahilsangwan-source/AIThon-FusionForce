package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Quiz Question Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {

    private UUID id;
    private UUID quizId;
    private String questionText;
    private String questionType;
    private String options;
    private String correctAnswer;
    private Integer points;

}
