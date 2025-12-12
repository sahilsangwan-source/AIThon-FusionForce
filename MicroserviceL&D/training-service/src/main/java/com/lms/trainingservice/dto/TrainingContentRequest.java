package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Training Content Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingContentRequest {

    @NotNull(message = "Module ID is required")
    private UUID moduleId;

    private String contentUrl;

    private String contentType;

    private Long fileSize;

    private String metadata;

}
