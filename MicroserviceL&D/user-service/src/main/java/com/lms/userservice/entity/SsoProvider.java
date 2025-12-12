package com.lms.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * SsoProvider Entity
 * Stores SSO provider configurations
 */
@Entity
@Table(name = "sso_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(columnDefinition = "jsonb")
    private String config;
}
