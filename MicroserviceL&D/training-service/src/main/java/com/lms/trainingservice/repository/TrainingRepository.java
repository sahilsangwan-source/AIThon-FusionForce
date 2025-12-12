package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Training entity
 */
@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID> {

    /**
     * Find trainings by category
     */
    Page<Training> findByCategory(String category, Pageable pageable);

    /**
     * Find trainings by difficulty level
     */
    Page<Training> findByDifficultyLevel(String difficultyLevel, Pageable pageable);

    /**
     * Find trainings by status
     */
    Page<Training> findByStatus(String status, Pageable pageable);

    /**
     * Find trainings by created_by user
     */
    Page<Training> findByCreatedBy(UUID createdBy, Pageable pageable);

    /**
     * Search trainings by title or description
     */
    @Query("SELECT t FROM Training t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Training> searchTrainings(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find published trainings
     */
    List<Training> findByStatusOrderByCreatedAtDesc(String status);

}
