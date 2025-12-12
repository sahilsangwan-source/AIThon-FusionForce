package com.lms.userservice.service;

import com.lms.userservice.dto.AuthResponse;
import com.lms.userservice.entity.User;
import com.lms.userservice.entity.UserSession;
import com.lms.userservice.exception.InvalidCredentialsException;
import com.lms.userservice.exception.InvalidTokenException;
import com.lms.userservice.repository.UserSessionRepository;
import com.lms.userservice.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for authentication operations
 */
@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private RedisService redisService;

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse authenticate(String email, String password, String ipAddress, String userAgent) {
        // Get user by email
        User user = userService.getUserByEmail(email);

        // Verify password
        if (!passwordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is active
        if (!user.getStatus().equals("ACTIVE")) {
            throw new InvalidCredentialsException("User account is not active");
        }

        // Generate tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(email);

        // Save session
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        userSessionRepository.save(session);

        log.info("User authenticated successfully: {}", email);

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenExpiration())
                .user(userService.convertToResponse(user))
                .build();
    }

    /**
     * Generate access token with claims
     */
    private String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toArray());
        return jwtService.generateAccessToken(user.getEmail(), claims);
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        // Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Extract email from refresh token
        String email = jwtService.extractEmail(refreshToken);

        // Get user
        User user = userService.getUserByEmail(email);

        // Check if user is active
        if (!user.getStatus().equals("ACTIVE")) {
            throw new InvalidCredentialsException("User account is not active");
        }

        // Generate new access token
        String newAccessToken = generateAccessToken(user);

        // Find and update session
        var sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setToken(newAccessToken);
            userSessionRepository.save(session);
        }

        log.info("Access token refreshed for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenExpiration())
                .user(userService.convertToResponse(user))
                .build();
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        return jwtService.extractEmail(token);
    }

    /**
     * Logout user
     */
    @Transactional
    public void logout(String accessToken) {
        var sessionOpt = userSessionRepository.findByToken(accessToken);
        if (sessionOpt.isPresent()) {
            userSessionRepository.deleteById(sessionOpt.get().getId());
            log.info("User logged out successfully");
        }
    }
}
