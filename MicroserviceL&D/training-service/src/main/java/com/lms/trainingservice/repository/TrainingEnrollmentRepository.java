package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.TrainingEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Training Enrollment operations
 */
@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, UUID> {

    /**
     * Find enrollment by user and training
     */
    Optional<TrainingEnrollment> findByUserIdAndTrainingId(UUID userId, UUID trainingId);

    /**
     * Find all enrollments for a user
     */
    Page<TrainingEnrollment> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all enrollments for a training
     */
    Page<TrainingEnrollment> findByTrainingId(UUID trainingId, Pageable pageable);

    /**
     * Find enrollments by user and status
     */
    Page<TrainingEnrollment> findByUserIdAndEnrollmentStatus(UUID userId, String status, Pageable pageable);

    /**
     * Count enrollments for a training
     */
    long countByTrainingId(UUID trainingId);

    /**
     * Count enrollments for a user
     */
    long countByUserId(UUID userId);

    /**
     * Check if user is enrolled in training
     */
    boolean existsByUserIdAndTrainingId(UUID userId, UUID trainingId);

    /**
     * Get enrollments with training details
     */
    @Query("SELECT e FROM TrainingEnrollment e JOIN FETCH e.training WHERE e.userId = :userId")
    Page<TrainingEnrollment> findByUserIdWithTraining(@Param("userId") UUID userId, Pageable pageable);
}
