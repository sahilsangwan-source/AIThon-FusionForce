package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Quiz Question Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionRequest {

    @NotNull(message = "Quiz ID is required")
    private UUID quizId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private String questionType;

    private String options;

    private String correctAnswer;

    private Integer points;

}
