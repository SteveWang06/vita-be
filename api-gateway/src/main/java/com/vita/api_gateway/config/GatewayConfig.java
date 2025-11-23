package com.vita.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("========================================");
        log.info("CREATING CUSTOM ROUTE LOCATOR");
        log.info("========================================");
        return builder.routes()
                .route("test_route", r -> r
                        .path("/test/**")
                        .uri("http://httpbin.org"))
                .route("auth_service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8082/auth"))
                .route("user_service", r -> r
                        .path("/api/v1/user/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8081/user"))
                .route("post_service", r -> r
                        .path("/api/v1/post/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8083/post"))
                .route("posts_service", r -> r
                        .path("/api/v1/posts/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri("http://localhost:8083/post"))
                .route("admin_service", r -> r
                        .path("/api/v1/admin/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8082"))
                .route("rental_service", r -> r
                        .path("/api/v1/rentals/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8084"))
                .route("media_service", r -> r
                        .path("/api/v1/media/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8085"))
                .build();
    }
    
    public GatewayConfig() {
        log.info("========================================");
        log.info("GatewayConfig CONSTRUCTOR CALLED");
        log.info("========================================");
    }
}
