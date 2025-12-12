package com.lms.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * TrainingModule Entity
 * Represents a module within a training
 */
@Entity
@Table(name = "training_modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingModule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", insertable = false, updatable = false)
    private Training training;

    @OneToMany(mappedBy = "moduleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrainingContent> contents;

}
