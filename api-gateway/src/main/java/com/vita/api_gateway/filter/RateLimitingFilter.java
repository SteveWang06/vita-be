package com.vita.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Rate Limiting Filter with DDoS protection
 * Features:
 * - Different rate limits for authenticated vs anonymous users
 * - Automatic cleanup of expired entries to prevent memory leaks
 * - Detailed logging and metrics
 * - Configurable limits per user type
 */
@Slf4j
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    @Value("${app.rate-limit.anonymous.requests-per-minute:60}")
    private int anonymousRequestsPerMinute;

    @Value("${app.rate-limit.authenticated.requests-per-minute:200}")
    private int authenticatedRequestsPerMinute;

    @Value("${app.rate-limit.cleanup-interval-minutes:5}")
    private int cleanupIntervalMinutes;

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // Schedule periodic cleanup to prevent memory leaks
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredEntries,
            cleanupIntervalMinutes,
            cleanupIntervalMinutes,
            TimeUnit.MINUTES
        );
        log.info("RateLimitingFilter initialized with cleanup interval: {} minutes", cleanupIntervalMinutes);
        log.info("Anonymous rate limit: {} requests/minute", anonymousRequestsPerMinute);
        log.info("Authenticated rate limit: {} requests/minute", authenticatedRequestsPerMinute);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Get client IP address
        String clientIp = getClientIp(request);
        
        // Check if user is authenticated
        String userId = request.getHeaders().getFirst("X-User-Id");
        boolean isAuthenticated = userId != null && !userId.isEmpty();
        
        // Create unique client identifier
        String clientId = isAuthenticated ? "AUTH:" + userId : "ANON:" + clientIp;
        
        // Determine rate limit based on authentication status
        int rateLimit = isAuthenticated ? authenticatedRequestsPerMinute : anonymousRequestsPerMinute;
        
        // Get or create rate limit info
        RateLimitInfo rateLimitInfo = rateLimitMap.compute(clientId, (key, existing) -> {
            if (existing == null || isWindowExpired(existing)) {
                return new RateLimitInfo(System.currentTimeMillis(), new AtomicInteger(1), isAuthenticated);
            } else {
                existing.counter.incrementAndGet();
                existing.lastAccessTime = System.currentTimeMillis();
                return existing;
            }
        });

        // Check if rate limit exceeded
        if (isRateLimitExceeded(rateLimitInfo, rateLimit)) {
            log.warn("Rate limit exceeded for client: {} (IP: {}, Type: {}, Count: {})", 
                clientId, clientIp, isAuthenticated ? "authenticated" : "anonymous", rateLimitInfo.counter.get());
            return onRateLimitExceeded(exchange, rateLimitInfo, rateLimit);
        }

        // Add rate limit headers to response
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(rateLimit));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
            String.valueOf(Math.max(0, rateLimit - rateLimitInfo.counter.get())));

        return chain.filter(exchange);
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header (for proxies)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    private boolean isRateLimitExceeded(RateLimitInfo info, int limit) {
        return info.counter.get() > limit;
    }

    private boolean isWindowExpired(RateLimitInfo info) {
        return System.currentTimeMillis() - info.windowStart > 60000; // 1 minute
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        long[] removedCount = {0};  // Use array to make it effectively final
        
        rateLimitMap.entrySet().removeIf(entry -> {
            boolean expired = now - entry.getValue().lastAccessTime > 300000; // 5 minutes
            if (expired) {
                removedCount[0]++;
            }
            return expired;
        });
        
        if (removedCount[0] > 0) {
            log.info("Cleaned up {} expired rate limit entries. Current size: {}", removedCount[0], rateLimitMap.size());
        }
    }

    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange, RateLimitInfo info, int limit) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf((info.windowStart + 60000) / 1000));
        
        String errorResponse = String.format(
            "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Limit: %d requests per minute. Please try again later.\", \"retryAfter\": 60}",
            limit
        );
        var buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -200; // Execute before authentication filter
    }

    private static class RateLimitInfo {
        final long windowStart;
        final AtomicInteger counter;
        volatile long lastAccessTime;

        RateLimitInfo(long windowStart, AtomicInteger counter, boolean isAuthenticated) {
            this.windowStart = windowStart;
            this.counter = counter;
            this.lastAccessTime = windowStart;
        }
    }
}
