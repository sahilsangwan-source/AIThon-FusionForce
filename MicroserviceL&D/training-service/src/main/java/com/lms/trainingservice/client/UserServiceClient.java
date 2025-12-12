package com.lms.trainingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Feign Client for User Service communication
 * Includes circuit breaker and retry patterns
 */
@FeignClient(name = "USER-SERVICE", fallback = UserServiceClient.UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    Map<String, Object> getUserById(@PathVariable("id") UUID id);

    @GetMapping("/api/users/email/{email}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "userService")
    Map<String, Object> getUserByEmail(@PathVariable("email") String email);

    /**
     * Fallback implementation when user-service is unavailable
     */
    @Component
    @Slf4j
    class UserServiceFallback implements UserServiceClient {

        @Override
        public Map<String, Object> getUserById(UUID id) {
            log.warn("Fallback: User service unavailable, returning default for user id: {}", id);
            Map<String, Object> fallbackUser = new HashMap<>();
            fallbackUser.put("id", id);
            fallbackUser.put("email", "unavailable@service.down");
            fallbackUser.put("firstName", "Service");
            fallbackUser.put("lastName", "Unavailable");
            fallbackUser.put("status", "UNKNOWN");
            return fallbackUser;
        }

        @Override
        public Map<String, Object> getUserByEmail(String email) {
            log.warn("Fallback: User service unavailable, returning default for email: {}", email);
            Map<String, Object> fallbackUser = new HashMap<>();
            fallbackUser.put("email", email);
            fallbackUser.put("status", "UNKNOWN");
            return fallbackUser;
        }
    }
}
