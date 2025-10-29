package com.vita.api_gateway.filter;

import lombok.RequiredArgsConstructor;
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
import java.util.List;

/**
 * Request Validation Filter for DDoS protection
 * Features:
 * - Request size validation (prevent large payload attacks)
 * - Header validation (prevent header manipulation attacks)
 * - URL length validation
 * - Suspicious pattern detection
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestValidationFilter implements GlobalFilter, Ordered {

    private final IpBlockingFilter ipBlockingFilter;

    @Value("${app.security.request-validation.enabled:true}")
    private boolean enabled;

    @Value("${app.security.request-validation.max-request-size:10485760}") // 10MB default
    private long maxRequestSize;

    @Value("${app.security.request-validation.max-url-length:2048}")
    private int maxUrlLength;

    @Value("${app.security.request-validation.max-header-size:8192}")
    private int maxHeaderSize;

    @Value("${app.security.request-validation.max-headers-count:50}")
    private int maxHeadersCount;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Request Validation Filter is DISABLED");
            return;
        }
        
        log.info("Request Validation Filter initialized");
        log.info("Max request size: {} bytes ({} MB)", maxRequestSize, maxRequestSize / 1024 / 1024);
        log.info("Max URL length: {} characters", maxUrlLength);
        log.info("Max header size: {} bytes", maxHeaderSize);
        log.info("Max headers count: {}", maxHeadersCount);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        // 1. Validate URL length
        String url = request.getURI().toString();
        if (url.length() > maxUrlLength) {
            log.warn("URL too long from IP {}: {} characters", clientIp, url.length());
            ipBlockingFilter.recordViolation(clientIp);
            return onValidationFailed(exchange, "URL length exceeds maximum allowed");
        }

        // 2. Validate headers count
        int headerCount = request.getHeaders().size();
        if (headerCount > maxHeadersCount) {
            log.warn("Too many headers from IP {}: {} headers", clientIp, headerCount);
            ipBlockingFilter.recordViolation(clientIp);
            return onValidationFailed(exchange, "Too many headers");
        }

        // 3. Validate total header size
        int totalHeaderSize = calculateHeaderSize(request);
        if (totalHeaderSize > maxHeaderSize) {
            log.warn("Headers too large from IP {}: {} bytes", clientIp, totalHeaderSize);
            ipBlockingFilter.recordViolation(clientIp);
            return onValidationFailed(exchange, "Headers size exceeds maximum allowed");
        }

        // 4. Validate Content-Length header
        String contentLength = request.getHeaders().getFirst("Content-Length");
        if (contentLength != null) {
            try {
                long size = Long.parseLong(contentLength);
                if (size > maxRequestSize) {
                    log.warn("Request body too large from IP {}: {} bytes", clientIp, size);
                    ipBlockingFilter.recordViolation(clientIp);
                    return onValidationFailed(exchange, "Request body size exceeds maximum allowed");
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid Content-Length header from IP {}: {}", clientIp, contentLength);
                ipBlockingFilter.recordViolation(clientIp);
                return onValidationFailed(exchange, "Invalid Content-Length header");
            }
        }

        // 5. Check for suspicious patterns in headers
        if (hasSuspiciousHeaders(request)) {
            log.warn("Suspicious headers detected from IP {}", clientIp);
            ipBlockingFilter.recordViolation(clientIp);
            return onValidationFailed(exchange, "Suspicious request detected");
        }

        // 6. Validate User-Agent (block requests without User-Agent or with suspicious ones)
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            log.debug("Request without User-Agent from IP {}", clientIp);
            // Don't block, but log for monitoring
        } else if (isSuspiciousUserAgent(userAgent)) {
            log.warn("Suspicious User-Agent from IP {}: {}", clientIp, userAgent);
            ipBlockingFilter.recordViolation(clientIp);
            return onValidationFailed(exchange, "Suspicious User-Agent detected");
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    private int calculateHeaderSize(ServerHttpRequest request) {
        int totalSize = 0;
        for (var entry : request.getHeaders().entrySet()) {
            totalSize += entry.getKey().length();
            for (String value : entry.getValue()) {
                totalSize += value.length();
            }
        }
        return totalSize;
    }

    private boolean hasSuspiciousHeaders(ServerHttpRequest request) {
        // Check for common attack patterns in headers
        List<String> suspiciousPatterns = List.of(
            "<script", "javascript:", "onerror=", "onload=",
            "../", "..\\", "<?php", "<?=",
            "union select", "' or '1'='1", "drop table"
        );

        for (var entry : request.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                String lowerValue = value.toLowerCase();
                for (String pattern : suspiciousPatterns) {
                    if (lowerValue.contains(pattern.toLowerCase())) {
                        log.warn("Suspicious pattern '{}' found in header '{}': {}", pattern, entry.getKey(), value);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSuspiciousUserAgent(String userAgent) {
        String lowerUserAgent = userAgent.toLowerCase();
        
        // Known malicious or suspicious user agents
        List<String> suspiciousAgents = List.of(
            "sqlmap", "nikto", "nmap", "masscan",
            "metasploit", "burpsuite", "havij",
            "acunetix", "netsparker", "zaproxy"
        );

        for (String suspicious : suspiciousAgents) {
            if (lowerUserAgent.contains(suspicious)) {
                return true;
            }
        }

        // Check for very short or very long user agents (potential attack)
        if (userAgent.length() < 10 || userAgent.length() > 512) {
            return true;
        }

        return false;
    }

    private Mono<Void> onValidationFailed(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format(
            "{\"error\": \"Request Validation Failed\", \"message\": \"%s\"}",
            reason
        );
        var buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -250; // Execute after IP blocking, before rate limiting
    }
}

