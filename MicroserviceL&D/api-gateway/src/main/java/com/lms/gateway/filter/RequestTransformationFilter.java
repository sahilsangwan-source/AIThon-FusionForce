package com.lms.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class RequestTransformationFilter extends AbstractGatewayFilterFactory<RequestTransformationFilter.Config> {

    public RequestTransformationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String originalPath = request.getURI().getPath();

            log.debug("Original request path: {}", originalPath);

            // This filter serves as an additional layer for custom transformations if needed
            // The path transformation is handled by StripPrefix filter in the route configuration

            return chain.filter(exchange);
        };
    }

    // Configuration properties if needed
    public static class Config {
    }
}
