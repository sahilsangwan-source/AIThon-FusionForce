package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.TrainingModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TrainingModule entity
 */
@Repository
public interface TrainingModuleRepository extends JpaRepository<TrainingModule, UUID> {

    /**
     * Find all modules for a training
     */
    List<TrainingModule> findByTrainingIdOrderBySequenceOrder(UUID trainingId);

}
