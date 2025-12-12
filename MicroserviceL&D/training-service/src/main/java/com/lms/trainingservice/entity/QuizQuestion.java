package com.lms.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * QuizQuestion Entity
 * Represents a question within a quiz
 */
@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_type", length = 20)
    private String questionType;

    @Column(columnDefinition = "jsonb")
    private String options;

    @Column(name = "correct_answer", columnDefinition = "jsonb")
    private String correctAnswer;

    @Column
    private Integer points;

}
