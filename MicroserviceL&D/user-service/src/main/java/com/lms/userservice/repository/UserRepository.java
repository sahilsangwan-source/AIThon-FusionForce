package com.lms.userservice.repository;

import com.lms.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by employee ID
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if employee ID exists
     */
    boolean existsByEmployeeId(String employeeId);
}
