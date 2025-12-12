package com.lms.userservice.kafka;

import com.lms.userservice.constant.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka producer for user events
 */
@Service
@Slf4j
public class UserEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish user registered event
     */
    public void publishUserRegistered(UUID userId, String email) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("userId", userId.toString());
        event.put("email", email);
        event.put("eventType", "USER_REGISTERED");
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(AppConstants.KAFKA_TOPIC_USER_REGISTERED, event);
        log.info("Published user registered event for user: {}", userId);
    }

    /**
     * Publish user updated event
     */
    public void publishUserUpdated(UUID userId, String email) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("userId", userId.toString());
        event.put("email", email);
        event.put("eventType", "USER_UPDATED");
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(AppConstants.KAFKA_TOPIC_USER_UPDATED, event);
        log.info("Published user updated event for user: {}", userId);
    }

    /**
     * Publish user deleted event
     */
    public void publishUserDeleted(UUID userId, String email) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("userId", userId.toString());
        event.put("email", email);
        event.put("eventType", "USER_DELETED");
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(AppConstants.KAFKA_TOPIC_USER_DELETED, event);
        log.info("Published user deleted event for user: {}", userId);
    }
}
