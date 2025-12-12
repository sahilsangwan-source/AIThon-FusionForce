package com.lms.trainingservice.service;

import com.lms.trainingservice.dto.QuizRequest;
import com.lms.trainingservice.dto.QuizResponse;
import com.lms.trainingservice.entity.Quiz;
import com.lms.trainingservice.exception.TrainingNotFoundException;
import com.lms.trainingservice.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for Quiz business logic
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;

    /**
     * Create a quiz for a module
     */
    public QuizResponse createQuiz(QuizRequest request) {
        log.info("Creating new quiz for module: {}", request.getModuleId());

        Quiz quiz = Quiz.builder()
                .moduleId(request.getModuleId())
                .title(request.getTitle())
                .passingScore(request.getPassingScore())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        log.info("Quiz created successfully with ID: {}", savedQuiz.getId());

        return convertToResponse(savedQuiz);
    }

    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(UUID id) {
        log.info("Fetching quiz with ID: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Quiz not found with ID: " + id));

        return convertToResponse(quiz);
    }

    /**
     * Update quiz
     */
    public QuizResponse updateQuiz(UUID id, QuizRequest request) {
        log.info("Updating quiz with ID: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Quiz not found with ID: " + id));

        quiz.setTitle(request.getTitle());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());

        Quiz updatedQuiz = quizRepository.save(quiz);
        log.info("Quiz updated successfully");

        return convertToResponse(updatedQuiz);
    }

    /**
     * Delete quiz
     */
    public void deleteQuiz(UUID id) {
        log.info("Deleting quiz with ID: {}", id);

        if (!quizRepository.existsById(id)) {
            throw new TrainingNotFoundException("Quiz not found with ID: " + id);
        }

        quizRepository.deleteById(id);
        log.info("Quiz deleted successfully");
    }

    /**
     * Get quizzes for a module
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByModule(UUID moduleId) {
        log.info("Fetching quizzes for module: {}", moduleId);
        return quizRepository.findByModuleId(moduleId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Convert Quiz entity to Response DTO
     */
    private QuizResponse convertToResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .moduleId(quiz.getModuleId())
                .title(quiz.getTitle())
                .passingScore(quiz.getPassingScore())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .build();
    }

}
