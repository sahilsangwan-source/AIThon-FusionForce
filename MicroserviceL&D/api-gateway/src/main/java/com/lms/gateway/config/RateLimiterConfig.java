package com.lms.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rate Limiter Configuration
 * Defines key resolvers for rate limiting
 */
@Configuration
public class RateLimiterConfig {

    /**
     * IP Address based rate limiter
     * Limits requests per IP address
     * @return KeyResolver for IP-based rate limiting
     */
    @Bean(name = "ipAddressKeyResolver")
    public KeyResolver ipAddressKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

    /**
     * User ID based rate limiter
     * Limits requests per authenticated user
     * @return KeyResolver for user-based rate limiting
     */
    @Bean(name = "userIdKeyResolver")
    public KeyResolver userIdKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-User-Id") != null ?
                        exchange.getRequest().getHeaders().getFirst("X-User-Id") :
                        Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

    /**
     * Route ID based rate limiter
     * Limits requests per route
     * @return KeyResolver for route-based rate limiting
     */
    @Bean(name = "routeKeyResolver")
    public KeyResolver routeKeyResolver() {
        return exchange -> Mono.just(
                exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute") != null ?
                        exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute").toString() :
                        "unknown"
        );
    }

    @Bean
    public GatewayFilter addCustomHeadersFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-Custom-Header", "CustomHeaderValue")
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    @Bean
    public GatewayFilter transformRequestFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String originalPath = request.getURI().getPath();

            // Transform the path for user-service and training-service
            if (originalPath.startsWith("/api/user")) {
                request = request.mutate().path(originalPath.replace("/api/user", "")).build();
            } else if (originalPath.startsWith("/api/training")) {
                request = request.mutate().path(originalPath.replace("/api/training", "")).build();
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }
}
