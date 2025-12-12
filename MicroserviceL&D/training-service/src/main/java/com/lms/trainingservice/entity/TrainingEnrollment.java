package com.lms.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's enrollment in a training
 */
@Entity
@Table(name = "training_enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "training_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "enrollment_status")
    private String enrollmentStatus; // ENROLLED, IN_PROGRESS, COMPLETED, DROPPED

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "enrolled_at")
    @CreationTimestamp
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
