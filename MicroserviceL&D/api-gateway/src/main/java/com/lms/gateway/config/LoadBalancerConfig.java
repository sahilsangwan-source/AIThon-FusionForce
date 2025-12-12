package com.lms.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Load Balancer Configuration for API Gateway
 * Configures load balancing for microservices communication
 */
@Configuration
@LoadBalancerClients({
        @LoadBalancerClient(name = "USER-SERVICE", configuration = LoadBalancerConfig.UserServiceLoadBalancerConfig.class),
        @LoadBalancerClient(name = "TRAINING-SERVICE", configuration = LoadBalancerConfig.TrainingServiceLoadBalancerConfig.class)
})
public class LoadBalancerConfig {

    /**
     * WebClient with load balancing support for reactive calls
     * Used by Gateway filters for inter-service communication
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Service instance list supplier for load balancing
     * Fetches available service instances from Eureka
     */
    @Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            org.springframework.context.ConfigurableApplicationContext context) {

        return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .build(context);
    }

    // ========================
    // USER-SERVICE LoadBalancer
    // ========================
    @Configuration
    public static class UserServiceLoadBalancerConfig {
        @Bean
        public ReactorLoadBalancer<?> userServiceLoadBalancer(LoadBalancerClientFactory factory) {

            return new RandomLoadBalancer(
                    factory.getLazyProvider("USER-SERVICE", ServiceInstanceListSupplier.class),
                    "USER-SERVICE"
            );
        }
    }

    // ========================
    // TRAINING-SERVICE LoadBalancer
    // ========================
    @Configuration
    public static class TrainingServiceLoadBalancerConfig {
        @Bean
        public ReactorLoadBalancer<?> trainingServiceLoadBalancer(LoadBalancerClientFactory factory) {

            return new RandomLoadBalancer(
                    factory.getLazyProvider("TRAINING-SERVICE", ServiceInstanceListSupplier.class),
                    "TRAINING-SERVICE"
            );
        }
    }
}