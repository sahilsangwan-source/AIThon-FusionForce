package com.lms.userservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign Client for Training Service communication
 * Includes circuit breaker and retry patterns
 */
@FeignClient(name = "TRAINING-SERVICE", fallback = TrainingServiceClient.TrainingServiceFallback.class)
public interface TrainingServiceClient {

    @GetMapping("/api/trainings/{id}")
    @CircuitBreaker(name = "trainingService", fallbackMethod = "getTrainingFallback")
    @Retry(name = "trainingService")
    Map<String, Object> getTrainingById(@PathVariable("id") UUID id);

    @GetMapping("/api/enrollments/my-trainings")
    @CircuitBreaker(name = "trainingService", fallbackMethod = "getUserEnrollmentsFallback")
    @Retry(name = "trainingService")
    Map<String, Object> getUserEnrollments(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size);

    /**
     * Fallback implementation when training-service is unavailable
     */
    @Component
    @Slf4j
    class TrainingServiceFallback implements TrainingServiceClient {

        @Override
        public Map<String, Object> getTrainingById(UUID id) {
            log.warn("Fallback: Training service unavailable, returning default for training id: {}", id);
            Map<String, Object> fallbackTraining = new HashMap<>();
            fallbackTraining.put("id", id);
            fallbackTraining.put("title", "Training Service Unavailable");
            fallbackTraining.put("status", "UNKNOWN");
            return fallbackTraining;
        }

        @Override
        public Map<String, Object> getUserEnrollments(int page, int size) {
            log.warn("Fallback: Training service unavailable, returning empty enrollments");
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("content", Collections.emptyList());
            fallbackResponse.put("totalElements", 0);
            fallbackResponse.put("totalPages", 0);
            return fallbackResponse;
        }
    }
}
