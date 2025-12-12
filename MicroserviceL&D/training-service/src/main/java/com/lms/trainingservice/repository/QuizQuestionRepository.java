package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for QuizQuestion entity
 */
@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

    /**
     * Find all questions for a quiz
     */
    List<QuizQuestion> findByQuizId(UUID quizId);

}
