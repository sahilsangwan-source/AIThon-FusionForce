package com.lms.userservice.repository;

import com.lms.userservice.entity.SsoProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SsoProvider entity
 */
@Repository
public interface SsoProviderRepository extends JpaRepository<SsoProvider, UUID> {

    /**
     * Find SSO provider by name
     */
    Optional<SsoProvider> findByProviderName(String providerName);

    /**
     * Check if provider exists by name
     */
    boolean existsByProviderName(String providerName);
}
