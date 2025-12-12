package com.lms.trainingservice.service;

import com.lms.trainingservice.dto.TrainingContentRequest;
import com.lms.trainingservice.dto.TrainingContentResponse;
import com.lms.trainingservice.entity.TrainingContent;
import com.lms.trainingservice.exception.TrainingNotFoundException;
import com.lms.trainingservice.repository.TrainingContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for Training Content business logic
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingContentService {

    private final TrainingContentRepository trainingContentRepository;

    /**
     * Create content for a module
     */
    public TrainingContentResponse createContent(TrainingContentRequest request) {
        log.info("Creating new content for module: {}", request.getModuleId());

        TrainingContent content = TrainingContent.builder()
                .moduleId(request.getModuleId())
                .contentUrl(request.getContentUrl())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .metadata(request.getMetadata())
                .build();

        TrainingContent savedContent = trainingContentRepository.save(content);
        log.info("Content created successfully with ID: {}", savedContent.getId());

        return convertToResponse(savedContent);
    }

    /**
     * Get content by ID
     */
    @Transactional(readOnly = true)
    public TrainingContentResponse getContentById(UUID id) {
        log.info("Fetching content with ID: {}", id);

        TrainingContent content = trainingContentRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Content not found with ID: " + id));

        return convertToResponse(content);
    }

    /**
     * Update content
     */
    public TrainingContentResponse updateContent(UUID id, TrainingContentRequest request) {
        log.info("Updating content with ID: {}", id);

        TrainingContent content = trainingContentRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Content not found with ID: " + id));

        content.setContentUrl(request.getContentUrl());
        content.setContentType(request.getContentType());
        content.setFileSize(request.getFileSize());
        content.setMetadata(request.getMetadata());

        TrainingContent updatedContent = trainingContentRepository.save(content);
        log.info("Content updated successfully");

        return convertToResponse(updatedContent);
    }

    /**
     * Delete content
     */
    public void deleteContent(UUID id) {
        log.info("Deleting content with ID: {}", id);

        if (!trainingContentRepository.existsById(id)) {
            throw new TrainingNotFoundException("Content not found with ID: " + id);
        }

        trainingContentRepository.deleteById(id);
        log.info("Content deleted successfully");
    }

    /**
     * Get all content for a module
     */
    @Transactional(readOnly = true)
    public List<TrainingContentResponse> getContentByModule(UUID moduleId) {
        log.info("Fetching content for module: {}", moduleId);
        return trainingContentRepository.findByModuleId(moduleId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Convert TrainingContent entity to Response DTO
     */
    private TrainingContentResponse convertToResponse(TrainingContent content) {
        return TrainingContentResponse.builder()
                .id(content.getId())
                .moduleId(content.getModuleId())
                .contentUrl(content.getContentUrl())
                .contentType(content.getContentType())
                .fileSize(content.getFileSize())
                .metadata(content.getMetadata())
                .build();
    }

}
