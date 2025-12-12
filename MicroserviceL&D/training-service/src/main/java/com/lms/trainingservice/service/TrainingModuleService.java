package com.lms.trainingservice.service;

import com.lms.trainingservice.dto.TrainingModuleRequest;
import com.lms.trainingservice.dto.TrainingModuleResponse;
import com.lms.trainingservice.entity.TrainingModule;
import com.lms.trainingservice.exception.TrainingNotFoundException;
import com.lms.trainingservice.repository.TrainingModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for Training Module business logic
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingModuleService {

    private final TrainingModuleRepository trainingModuleRepository;

    /**
     * Create a new module for a training
     */
    public TrainingModuleResponse createModule(TrainingModuleRequest request) {
        log.info("Creating new module for training: {}", request.getTrainingId());

        TrainingModule module = TrainingModule.builder()
                .trainingId(request.getTrainingId())
                .title(request.getTitle())
                .description(request.getDescription())
                .sequenceOrder(request.getSequenceOrder())
                .contentType(request.getContentType())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .build();

        TrainingModule savedModule = trainingModuleRepository.save(module);
        log.info("Module created successfully with ID: {}", savedModule.getId());

        return convertToResponse(savedModule);
    }

    /**
     * Get module by ID
     */
    @Transactional(readOnly = true)
    public TrainingModuleResponse getModuleById(UUID id) {
        log.info("Fetching module with ID: {}", id);

        TrainingModule module = trainingModuleRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Module not found with ID: " + id));

        return convertToResponse(module);
    }

    /**
     * Update module
     */
    public TrainingModuleResponse updateModule(UUID id, TrainingModuleRequest request) {
        log.info("Updating module with ID: {}", id);

        TrainingModule module = trainingModuleRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Module not found with ID: " + id));

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setSequenceOrder(request.getSequenceOrder());
        module.setContentType(request.getContentType());
        module.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());

        TrainingModule updatedModule = trainingModuleRepository.save(module);
        log.info("Module updated successfully");

        return convertToResponse(updatedModule);
    }

    /**
     * Delete module
     */
    public void deleteModule(UUID id) {
        log.info("Deleting module with ID: {}", id);

        if (!trainingModuleRepository.existsById(id)) {
            throw new TrainingNotFoundException("Module not found with ID: " + id);
        }

        trainingModuleRepository.deleteById(id);
        log.info("Module deleted successfully");
    }

    /**
     * Get all modules for a training
     */
    @Transactional(readOnly = true)
    public List<TrainingModuleResponse> getModulesByTraining(UUID trainingId) {
        log.info("Fetching modules for training: {}", trainingId);
        return trainingModuleRepository.findByTrainingIdOrderBySequenceOrder(trainingId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Convert TrainingModule entity to Response DTO
     */
    private TrainingModuleResponse convertToResponse(TrainingModule module) {
        return TrainingModuleResponse.builder()
                .id(module.getId())
                .trainingId(module.getTrainingId())
                .title(module.getTitle())
                .description(module.getDescription())
                .sequenceOrder(module.getSequenceOrder())
                .contentType(module.getContentType())
                .estimatedDurationMinutes(module.getEstimatedDurationMinutes())
                .build();
    }

}
