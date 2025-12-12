package com.lms.trainingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter for Training Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // First check if request came through API Gateway (has X-User-* headers)
            String gatewayUserId = request.getHeader("X-User-Id");
            String gatewayUserEmail = request.getHeader("X-User-Email");
            String gatewayUserRole = request.getHeader("X-User-Role");

            if (StringUtils.hasText(gatewayUserId) && StringUtils.hasText(gatewayUserEmail)) {
                // Request authenticated by gateway, use headers
                log.debug("Using gateway authentication headers for user: {}", gatewayUserEmail);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        gatewayUserEmail, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + gatewayUserRole)));
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                request.setAttribute("userId", gatewayUserId);
                request.setAttribute("userEmail", gatewayUserEmail);
                request.setAttribute("userRole", gatewayUserRole);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Direct request, validate JWT
                String jwt = getJwtFromRequest(request);

                if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
                    String email = jwtService.extractEmail(jwt);
                    String userId = jwtService.extractUserId(jwt);
                    String role = jwtService.extractRole(jwt);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);
                    request.setAttribute("userRole", role);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication successful for user: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT from request
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
