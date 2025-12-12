package com.lms.trainingservice.controller;

import com.lms.trainingservice.dto.EnrollmentRequest;
import com.lms.trainingservice.dto.EnrollmentResponse;
import com.lms.trainingservice.service.TrainingEnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Training Enrollment APIs
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class TrainingEnrollmentController {

    private final TrainingEnrollmentService enrollmentService;

    /**
     * Enroll current user in a training
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollInTraining(
            @Valid @RequestBody EnrollmentRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enrollUser(userId, request));
    }

    /**
     * Get all enrollments for current user
     */
    @GetMapping("/my-trainings")
    public ResponseEntity<Page<EnrollmentResponse>> getMyEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(enrollmentService.getUserEnrollments(userId, pageable));
    }

    /**
     * Get users enrolled in a specific training (admin/instructor)
     */
    @GetMapping("/training/{trainingId}")
    public ResponseEntity<Page<EnrollmentResponse>> getTrainingEnrollments(
            @PathVariable UUID trainingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(enrollmentService.getTrainingEnrollments(trainingId, pageable));
    }

    /**
     * Get enrollment details for current user and training
     */
    @GetMapping("/training/{trainingId}/my-enrollment")
    public ResponseEntity<EnrollmentResponse> getMyEnrollment(
            @PathVariable UUID trainingId,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        return ResponseEntity.ok(enrollmentService.getEnrollment(userId, trainingId));
    }

    /**
     * Update progress for current user's enrollment
     */
    @PatchMapping("/training/{trainingId}/progress")
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @PathVariable UUID trainingId,
            @RequestParam Integer progressPercentage,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        return ResponseEntity.ok(enrollmentService.updateProgress(userId, trainingId, progressPercentage));
    }

    /**
     * Unenroll current user from a training
     */
    @DeleteMapping("/training/{trainingId}")
    public ResponseEntity<Void> unenrollFromTraining(
            @PathVariable UUID trainingId,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        enrollmentService.unenrollUser(userId, trainingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if current user is enrolled in a training
     */
    @GetMapping("/training/{trainingId}/is-enrolled")
    public ResponseEntity<Boolean> isEnrolled(
            @PathVariable UUID trainingId,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        return ResponseEntity.ok(enrollmentService.isUserEnrolled(userId, trainingId));
    }
}
