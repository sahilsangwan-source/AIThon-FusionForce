package com.lms.trainingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka Configuration for Training Service
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    // Kafka configuration is handled via application.yml

}
