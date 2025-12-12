package com.lms.userservice.controller;

import com.lms.userservice.dto.*;
import com.lms.userservice.entity.User;
import com.lms.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for user management endpoints
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register new user endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequest request) {

        User user = userService.registerUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmployeeId(),
            request.getDepartment()
        );

        UserResponse response = userService.convertToResponse(user);
        log.info("User registered successfully: {}", request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "User registered successfully", response));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        UserResponse response = userService.convertToResponse(user);

        return ResponseEntity.ok(
            ApiResponse.success(200, "User profile retrieved", response)
        );
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        UserResponse response = userService.convertToResponse(user);

        return ResponseEntity.ok(
            ApiResponse.success(200, "User retrieved", response)
        );
    }

    /**
     * Update user profile
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        User user = userService.getUserByEmail(authentication.getName());
        User updatedUser = userService.updateUser(
            user.getId(),
            request.getFirstName(),
            request.getLastName(),
            request.getDepartment()
        );

        UserResponse response = userService.convertToResponse(updatedUser);
        log.info("User profile updated: {}", user.getId());

        return ResponseEntity.ok(
            ApiResponse.success(200, "User profile updated", response)
        );
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
            .map(userService::convertToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(
            ApiResponse.success(200, "Users retrieved", responses)
        );
    }

    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        log.info("User deleted: {}", id);

        return ResponseEntity.ok(
            ApiResponse.success(200, "User deleted successfully", null)
        );
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }
}
