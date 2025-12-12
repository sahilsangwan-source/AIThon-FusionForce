package com.lms.userservice.service;

import com.lms.userservice.dto.UserResponse;
import com.lms.userservice.entity.Role;
import com.lms.userservice.entity.User;
import com.lms.userservice.exception.EmailAlreadyExistsException;
import com.lms.userservice.exception.UserNotFoundException;
import com.lms.userservice.kafka.UserEventProducer;
import com.lms.userservice.repository.RoleRepository;
import com.lms.userservice.repository.UserRepository;
import com.lms.userservice.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for user management
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserEventProducer userEventProducer;

    @Autowired
    private RedisService redisService;

    /**
     * Register new user
     */
    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName,
                           String employeeId, String department) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }

        // Check if employee ID already exists
        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new EmailAlreadyExistsException("Employee ID already exists: " + employeeId);
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordUtil.encodePassword(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmployeeId(employeeId);
        user.setDepartment(department);
        user.setStatus("ACTIVE");

        // Add default EMPLOYEE role
        Optional<Role> employeeRole = roleRepository.findByName("EMPLOYEE");
        if (employeeRole.isPresent()) {
            user.getRoles().add(employeeRole.get());
        }

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", email);

        // Publish event
        userEventProducer.publishUserRegistered(savedUser.getId(), email);

        return savedUser;
    }

    /**
     * Get user by ID
     */
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Get user by employee ID
     */
    public User getUserByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User not found with employee ID: " + employeeId));
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateUser(UUID userId, String firstName, String lastName, String department) {
        User user = getUserById(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDepartment(department);
        
        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", userId);

        // Publish event
        userEventProducer.publishUserUpdated(userId, user.getEmail());

        // Clear cache
        redisService.deleteCachedUser(userId.toString());

        return updatedUser;
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("User deleted: {}", userId);

        // Publish event
        userEventProducer.publishUserDeleted(userId, user.getEmail());

        // Clear cache
        redisService.deleteCachedUser(userId.toString());
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Convert user entity to response DTO
     */
    public UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
