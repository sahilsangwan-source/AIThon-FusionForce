package com.lms.userservice.repository;

import com.lms.userservice.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSession entity
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Find session by token
     */
    Optional<UserSession> findByToken(String token);

    /**
     * Find session by refresh token
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Find all sessions for a user
     */
    List<UserSession> findByUserId(UUID userId);
}
