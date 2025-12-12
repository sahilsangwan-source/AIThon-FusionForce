package com.lms.trainingservice.service;

import com.lms.trainingservice.dto.EnrollmentRequest;
import com.lms.trainingservice.dto.EnrollmentResponse;
import com.lms.trainingservice.entity.Training;
import com.lms.trainingservice.entity.TrainingEnrollment;
import com.lms.trainingservice.exception.TrainingException;
import com.lms.trainingservice.exception.TrainingNotFoundException;
import com.lms.trainingservice.repository.TrainingEnrollmentRepository;
import com.lms.trainingservice.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing training enrollments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingEnrollmentService {

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingRepository trainingRepository;

    /**
     * Enroll a user in a training
     */
    @Transactional
    public EnrollmentResponse enrollUser(UUID userId, EnrollmentRequest request) {
        log.info("Enrolling user {} in training {}", userId, request.getTrainingId());

        // Check if training exists
        Training training = trainingRepository.findById(request.getTrainingId())
                .orElseThrow(() -> new TrainingNotFoundException("Training not found with id: " + request.getTrainingId()));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndTrainingId(userId, request.getTrainingId())) {
            throw new TrainingException("User is already enrolled in this training");
        }

        // Create enrollment
        TrainingEnrollment enrollment = TrainingEnrollment.builder()
                .userId(userId)
                .training(training)
                .enrollmentStatus(request.getEnrollmentStatus() != null ? request.getEnrollmentStatus() : "ENROLLED")
                .progressPercentage(0)
                .enrolledAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} successfully enrolled in training {}", userId, request.getTrainingId());

        return mapToResponse(enrollment);
    }

    /**
     * Get all enrollments for a user
     */
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getUserEnrollments(UUID userId, Pageable pageable) {
        log.info("Fetching enrollments for user {}", userId);
        Page<TrainingEnrollment> enrollments = enrollmentRepository.findByUserIdWithTraining(userId, pageable);
        return enrollments.map(this::mapToResponse);
    }

    /**
     * Get users enrolled in a training
     */
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getTrainingEnrollments(UUID trainingId, Pageable pageable) {
        log.info("Fetching enrollments for training {}", trainingId);
        Page<TrainingEnrollment> enrollments = enrollmentRepository.findByTrainingId(trainingId, pageable);
        return enrollments.map(this::mapToResponse);
    }

    /**
     * Update enrollment progress
     */
    @Transactional
    public EnrollmentResponse updateProgress(UUID userId, UUID trainingId, Integer progressPercentage) {
        log.info("Updating progress for user {} in training {} to {}%", userId, trainingId, progressPercentage);

        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new TrainingNotFoundException("Enrollment not found"));

        enrollment.setProgressPercentage(progressPercentage);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        if (progressPercentage >= 100) {
            enrollment.setEnrollmentStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());
        } else if (progressPercentage > 0) {
            enrollment.setEnrollmentStatus("IN_PROGRESS");
        }

        enrollment = enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    /**
     * Unenroll a user from a training
     */
    @Transactional
    public void unenrollUser(UUID userId, UUID trainingId) {
        log.info("Unenrolling user {} from training {}", userId, trainingId);

        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new TrainingNotFoundException("Enrollment not found"));

        enrollmentRepository.delete(enrollment);
        log.info("User {} successfully unenrolled from training {}", userId, trainingId);
    }

    /**
     * Get enrollment details
     */
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(UUID userId, UUID trainingId) {
        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new TrainingNotFoundException("Enrollment not found"));
        return mapToResponse(enrollment);
    }

    /**
     * Check if user is enrolled
     */
    @Transactional(readOnly = true)
    public boolean isUserEnrolled(UUID userId, UUID trainingId) {
        return enrollmentRepository.existsByUserIdAndTrainingId(userId, trainingId);
    }

    /**
     * Map entity to response DTO
     */
    private EnrollmentResponse mapToResponse(TrainingEnrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .trainingId(enrollment.getTraining().getId())
                .trainingTitle(enrollment.getTraining().getTitle())
                .enrollmentStatus(enrollment.getEnrollmentStatus())
                .progressPercentage(enrollment.getProgressPercentage())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .build();
    }
}
