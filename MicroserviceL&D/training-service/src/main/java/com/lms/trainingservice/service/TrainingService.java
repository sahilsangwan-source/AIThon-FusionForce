package com.lms.trainingservice.service;

import com.lms.trainingservice.dto.TrainingRequest;
import com.lms.trainingservice.dto.TrainingResponse;
import com.lms.trainingservice.entity.Training;
import com.lms.trainingservice.exception.TrainingNotFoundException;
import com.lms.trainingservice.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Training business logic
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create a new training
     */
    public TrainingResponse createTraining(TrainingRequest request, UUID createdBy) {
        log.info("Creating new training: {}", request.getTitle());

        Training training = Training.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficultyLevel(request.getDifficultyLevel())
                .durationHours(request.getDurationHours())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .createdBy(createdBy)
                .build();

        Training savedTraining = trainingRepository.save(training);
        log.info("Training created successfully with ID: {}", savedTraining.getId());

        // Publish event
        publishTrainingEvent("training.created", savedTraining.getId());

        return convertToResponse(savedTraining);
    }

    /**
     * Get training by ID
     */
    @Transactional(readOnly = true)
    public TrainingResponse getTrainingById(UUID id) {
        log.info("Fetching training with ID: {}", id);

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Training not found with ID: " + id));

        return convertToResponse(training);
    }

    /**
     * Update training
     */
    public TrainingResponse updateTraining(UUID id, TrainingRequest request) {
        log.info("Updating training with ID: {}", id);

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Training not found with ID: " + id));

        training.setTitle(request.getTitle());
        training.setDescription(request.getDescription());
        training.setCategory(request.getCategory());
        training.setDifficultyLevel(request.getDifficultyLevel());
        training.setDurationHours(request.getDurationHours());
        training.setThumbnailUrl(request.getThumbnailUrl());
        
        if (request.getStatus() != null) {
            training.setStatus(request.getStatus());
        }

        Training updatedTraining = trainingRepository.save(training);
        log.info("Training updated successfully");

        // Publish event
        publishTrainingEvent("training.updated", updatedTraining.getId());

        return convertToResponse(updatedTraining);
    }

    /**
     * Delete training
     */
    public void deleteTraining(UUID id) {
        log.info("Deleting training with ID: {}", id);

        if (!trainingRepository.existsById(id)) {
            throw new TrainingNotFoundException("Training not found with ID: " + id);
        }

        trainingRepository.deleteById(id);
        log.info("Training deleted successfully");

        // Publish event
        publishTrainingEvent("training.deleted", id);
    }

    /**
     * Get all trainings with pagination
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getAllTrainings(Pageable pageable) {
        log.info("Fetching all trainings");
        return trainingRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get trainings by category
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTrainingsByCategory(String category, Pageable pageable) {
        log.info("Fetching trainings by category: {}", category);
        return trainingRepository.findByCategory(category, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get trainings by difficulty level
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTrainingsByDifficultyLevel(String difficultyLevel, Pageable pageable) {
        log.info("Fetching trainings by difficulty level: {}", difficultyLevel);
        return trainingRepository.findByDifficultyLevel(difficultyLevel, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get trainings by status
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTrainingsByStatus(String status, Pageable pageable) {
        log.info("Fetching trainings by status: {}", status);
        return trainingRepository.findByStatus(status, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get trainings created by a user
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getTrainingsByCreator(UUID createdBy, Pageable pageable) {
        log.info("Fetching trainings created by user: {}", createdBy);
        return trainingRepository.findByCreatedBy(createdBy, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Search trainings
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> searchTrainings(String searchTerm, Pageable pageable) {
        log.info("Searching trainings with term: {}", searchTerm);
        return trainingRepository.searchTrainings(searchTerm, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get published trainings
     */
    @Transactional(readOnly = true)
    public Page<TrainingResponse> getPublishedTrainings(Pageable pageable) {
        log.info("Fetching published trainings");
        return trainingRepository.findByStatus("PUBLISHED", pageable)
                .map(this::convertToResponse);
    }

    /**
     * Publish a training (change status to PUBLISHED)
     */
    public TrainingResponse publishTraining(UUID id) {
        log.info("Publishing training with ID: {}", id);

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Training not found with ID: " + id));

        training.setStatus("PUBLISHED");
        Training publishedTraining = trainingRepository.save(training);
        log.info("Training published successfully");

        // Publish event
        publishTrainingEvent("training.published", publishedTraining.getId());

        return convertToResponse(publishedTraining);
    }

    /**
     * Convert Training entity to Response DTO
     */
    private TrainingResponse convertToResponse(Training training) {
        return TrainingResponse.builder()
                .id(training.getId())
                .title(training.getTitle())
                .description(training.getDescription())
                .category(training.getCategory())
                .difficultyLevel(training.getDifficultyLevel())
                .durationHours(training.getDurationHours())
                .thumbnailUrl(training.getThumbnailUrl())
                .createdBy(training.getCreatedBy())
                .status(training.getStatus())
                .createdAt(training.getCreatedAt())
                .updatedAt(training.getUpdatedAt())
                .build();
    }

    /**
     * Publish events to Kafka
     */
    private void publishTrainingEvent(String topic, UUID trainingId) {
        try {
            kafkaTemplate.send(topic, trainingId.toString());
            log.info("Published event to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish event to topic: {}", topic, e);
        }
    }

}
