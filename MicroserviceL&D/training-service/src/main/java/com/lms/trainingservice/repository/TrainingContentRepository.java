package com.lms.trainingservice.repository;

import com.lms.trainingservice.entity.TrainingContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TrainingContent entity
 */
@Repository
public interface TrainingContentRepository extends JpaRepository<TrainingContent, UUID> {

    /**
     * Find all content for a module
     */
    List<TrainingContent> findByModuleId(UUID moduleId);

}
