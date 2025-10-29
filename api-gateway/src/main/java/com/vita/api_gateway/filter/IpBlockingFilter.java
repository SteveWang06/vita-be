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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP Blocking Filter for DDoS protection
 * Features:
 * - Blacklist suspicious IPs
 * - Whitelist trusted IPs
 * - Automatic IP blocking after excessive violations
 * - Temporary and permanent blocking support
 */
@Slf4j
@Component
public class IpBlockingFilter implements GlobalFilter, Ordered {

    @Value("${app.security.ip-blocking.enabled:true}")
    private boolean enabled;

    @Value("${app.security.ip-blocking.whitelist:}")
    private Set<String> whitelistedIps;

    @Value("${app.security.ip-blocking.blacklist:}")
    private Set<String> blacklistedIps;

    // Runtime blacklist for dynamic blocking
    private final Set<String> runtimeBlacklist = ConcurrentHashMap.newKeySet();
    
    // Track violations per IP
    private final ConcurrentHashMap<String, Integer> violationCount = new ConcurrentHashMap<>();
    
    @Value("${app.security.ip-blocking.max-violations:10}")
    private int maxViolations;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("IP Blocking Filter is DISABLED");
            return;
        }
        
        log.info("IP Blocking Filter initialized");
        log.info("Whitelisted IPs: {}", whitelistedIps.isEmpty() ? "None" : whitelistedIps);
        log.info("Blacklisted IPs: {}", blacklistedIps.isEmpty() ? "None" : blacklistedIps);
        log.info("Max violations before auto-block: {}", maxViolations);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        // Check if IP is whitelisted (bypass all checks)
        if (isWhitelisted(clientIp)) {
            log.debug("Whitelisted IP allowed: {}", clientIp);
            return chain.filter(exchange);
        }

        // Check if IP is blacklisted
        if (isBlacklisted(clientIp)) {
            log.warn("Blocked blacklisted IP: {} attempting to access: {}", clientIp, request.getPath());
            return onIpBlocked(exchange, clientIp, "IP address is blacklisted");
        }

        // Check runtime blacklist (dynamic blocking)
        if (runtimeBlacklist.contains(clientIp)) {
            log.warn("Blocked dynamically blacklisted IP: {} attempting to access: {}", clientIp, request.getPath());
            return onIpBlocked(exchange, clientIp, "IP address has been temporarily blocked due to suspicious activity");
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header (for proxies/load balancers)
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

    private boolean isWhitelisted(String ip) {
        return whitelistedIps != null && whitelistedIps.contains(ip);
    }

    private boolean isBlacklisted(String ip) {
        return (blacklistedIps != null && blacklistedIps.contains(ip));
    }

    /**
     * Block an IP temporarily due to violations
     */
    public void blockIpTemporarily(String ip) {
        if (!isWhitelisted(ip)) {
            runtimeBlacklist.add(ip);
            log.warn("IP temporarily blocked: {}", ip);
        }
    }

    /**
     * Unblock a temporarily blocked IP
     */
    public void unblockIp(String ip) {
        if (runtimeBlacklist.remove(ip)) {
            log.info("IP unblocked: {}", ip);
        }
        violationCount.remove(ip);
    }

    /**
     * Record a violation for an IP and auto-block if threshold exceeded
     */
    public void recordViolation(String ip) {
        if (isWhitelisted(ip)) {
            return; // Don't track violations for whitelisted IPs
        }

        int violations = violationCount.merge(ip, 1, Integer::sum);
        log.warn("Violation recorded for IP: {} (Total: {})", ip, violations);

        if (violations >= maxViolations) {
            blockIpTemporarily(ip);
            log.error("IP auto-blocked due to excessive violations: {} (Violations: {})", ip, violations);
        }
    }

    /**
     * Get current violation count for an IP
     */
    public int getViolationCount(String ip) {
        return violationCount.getOrDefault(ip, 0);
    }

    /**
     * Check if an IP is currently blocked
     */
    public boolean isIpBlocked(String ip) {
        return isBlacklisted(ip) || runtimeBlacklist.contains(ip);
    }

    private Mono<Void> onIpBlocked(ServerWebExchange exchange, String ip, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format(
            "{\"error\": \"Access Denied\", \"message\": \"%s\", \"ip\": \"%s\"}",
            reason, ip
        );
        var buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -300; // Execute before rate limiting
    }
}

