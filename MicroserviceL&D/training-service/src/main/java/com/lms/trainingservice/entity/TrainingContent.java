package com.lms.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * TrainingContent Entity
 * Represents actual content within a module
 */
@Entity
@Table(name = "training_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(columnDefinition = "jsonb")
    private String metadata;

}
