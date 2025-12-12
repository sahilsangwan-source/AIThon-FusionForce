package com.lms.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service Application
 * 
 * Responsibilities:
 * - User authentication and authorization
 * - JWT token generation and validation
 * - User profile management
 * - Role-based access control (RBAC)
 * - SSO integration (OAuth2/SAML)
 * 
 * Port: 8081
 * 
 * @author LMS Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}