package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Quiz entity
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    /**
     * Find quizzes for a module
     */
    List<Quiz> findByModuleId(UUID moduleId);

}
