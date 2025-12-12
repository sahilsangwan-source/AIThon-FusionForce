package com.lms.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Training Content Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingContentResponse {

    private UUID id;
    private UUID moduleId;
    private String contentUrl;
    private String contentType;
    private Long fileSize;
    private String metadata;

}
