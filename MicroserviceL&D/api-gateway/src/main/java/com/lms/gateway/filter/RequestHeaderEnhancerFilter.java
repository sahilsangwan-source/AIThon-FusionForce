package com.lms.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Request Header Enhancer Filter
 * Adds additional security headers and marks requests as authenticated
 * Prevents direct access attempts to microservices
 */
@Component
@Slf4j
public class RequestHeaderEnhancerFilter extends AbstractGatewayFilterFactory<RequestHeaderEnhancerFilter.Config> {

    public RequestHeaderEnhancerFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Add a custom header to mark requests coming from API Gateway
            // Microservices can validate this header and reject direct access attempts
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Gateway-Authenticated", "true")
                    .header("X-Request-Source", "API-GATEWAY")
                    .header("X-Forwarded-By", "api-gateway")
                    .build();

            log.debug("Enhanced request headers - added gateway authentication markers");

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}

