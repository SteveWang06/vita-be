package com.vita.api_gateway.filter;

import com.vita.api_gateway.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final TokenValidationService tokenValidationService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();
        
        log.info("=== AuthenticationFilter: Incoming request === Path: {}, Method: {}", path, method);
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path, method)) {
            log.info("=== Public endpoint detected, skipping authentication === Path: {}", path);
            return chain.filter(exchange);
        }
        
        log.info("=== Protected endpoint, checking authentication === Path: {}", path);

        String authHeader = request.getHeaders().getFirst("Authorization");
        log.info("=== Authorization header: {} ===", authHeader != null ? "Present (Bearer ...)" : "MISSING");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("=== UNAUTHORIZED: Missing or invalid Authorization header ===");
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        log.info("=== Validating token (first 20 chars): {}... ===", token.substring(0, Math.min(20, token.length())));

        return tokenValidationService.validateToken(token)
                .flatMap(userInfo -> {
                    log.info("Token validated successfully. UserId: {}, Username: {}, Role: '{}'", 
                            userInfo.getUserId(), userInfo.getUsername(), userInfo.getRole());
                    
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userInfo.getUserId())
                            .header("X-User-Role", userInfo.getRole())
                            .header("X-Username", userInfo.getUsername())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("=== TOKEN VALIDATION FAILED === Path: {}, Error: {}, Type: {}", 
                            path, e.getMessage(), e.getClass().getSimpleName());
                    e.printStackTrace(); // Print full stack trace for debugging
                    return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
                });
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        // Public endpoints that don't require authentication
        if (path.startsWith("/api/v1/auth/") || 
            path.startsWith("/actuator/") ||
            path.equals("/health") ||
            path.equals("/info")) {
            return true;
        }
        
        // Allow GET requests for posts (read-only operations are public)
        // POST/PUT/DELETE require authentication
        if (path.startsWith("/api/v1/post") && HttpMethod.GET.equals(method)) {
            return true;
        }
        
        // Allow GET requests for rentals (read-only operations are public)
        // Includes: get all rentals, get by ID, search, get reviews, get average rating
        // POST/PUT/PATCH/DELETE require authentication
        // EXCEPT: /api/v1/rentals/admin/** endpoints (always require authentication)
        if (path.startsWith("/api/v1/rentals") && HttpMethod.GET.equals(method)) {
            // Admin endpoints always require authentication
            if (path.startsWith("/api/v1/rentals/admin")) {
                return false;
            }
            return true;
        }
        
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("=== SENDING ERROR RESPONSE === Status: {}, Message: {}, Path: {}", 
                status, message, exchange.getRequest().getPath().value());
        
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        var buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
