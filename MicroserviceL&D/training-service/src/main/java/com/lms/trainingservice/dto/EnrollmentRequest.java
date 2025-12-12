package com.lms.trainingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for enrollment request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotNull(message = "Training ID is required")
    private UUID trainingId;

    private String enrollmentStatus; // Optional: defaults to ENROLLED
}
