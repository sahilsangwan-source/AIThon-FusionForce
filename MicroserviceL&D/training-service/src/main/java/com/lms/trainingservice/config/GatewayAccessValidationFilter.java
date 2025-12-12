package com.lms.trainingservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

/**
 * Gateway Access Validation Filter
 * Ensures all requests come through the API Gateway
 * Rejects direct access attempts to the training-service
 */
@Component
@Slf4j
public class GatewayAccessValidationFilter extends OncePerRequestFilter {

    @Value("${gateway.require-gateway-auth:true}")
    private boolean requireGatewayAuth;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip validation for health checks and actuator endpoints
        if (requestPath.contains("/health") || requestPath.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for gateway authentication header
        String gatewayAuth = request.getHeader("X-Gateway-Authenticated");
        String requestSource = request.getHeader("X-Request-Source");

        if (requireGatewayAuth) {
            if (!"true".equals(gatewayAuth) || !"API-GATEWAY".equals(requestSource)) {
                log.warn("Rejected request not from API Gateway - Path: {}, Remote Address: {}",
                        requestPath, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Access Denied\", \"message\": \"Requests must come through API Gateway\"}");
                return;
            }
        }

        log.debug("Gateway validated request - Path: {}", requestPath);
        filterChain.doFilter(request, response);
    }
}

