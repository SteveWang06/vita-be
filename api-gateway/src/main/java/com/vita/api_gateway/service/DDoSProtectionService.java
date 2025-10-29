package com.vita.api_gateway.service;

import com.vita.api_gateway.filter.IpBlockingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Centralized DDoS Protection Service
 * Features:
 * - Monitor attack patterns
 * - Automatic threat detection
 * - Statistics and metrics
 * - Alert generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DDoSProtectionService {

    private final IpBlockingFilter ipBlockingFilter;

    // Statistics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong blockedRequests = new AtomicLong(0);
    private final AtomicLong rateLimitedRequests = new AtomicLong(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    // Attack detection
    private final ConcurrentHashMap<String, AttackInfo> suspiciousIps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> recentBlockedIps = new ConcurrentHashMap<>();

    /**
     * Record a request
     */
    public void recordRequest(String ip) {
        totalRequests.incrementAndGet();
        
        // Track suspicious IPs
        suspiciousIps.compute(ip, (key, existing) -> {
            if (existing == null) {
                return new AttackInfo(1, System.currentTimeMillis());
            } else {
                existing.count++;
                existing.lastSeen = System.currentTimeMillis();
                return existing;
            }
        });
    }

    /**
     * Record a blocked request
     */
    public void recordBlockedRequest(String ip, String reason) {
        blockedRequests.incrementAndGet();
        recentBlockedIps.put(ip, System.currentTimeMillis());
        log.warn("Request blocked from IP: {} - Reason: {}", ip, reason);
    }

    /**
     * Record a rate-limited request
     */
    public void recordRateLimitedRequest(String ip) {
        rateLimitedRequests.incrementAndGet();
        
        // Escalate to IP blocking if too many rate limit violations
        AttackInfo info = suspiciousIps.get(ip);
        if (info != null && info.count > 100) { // Threshold for escalation
            ipBlockingFilter.blockIpTemporarily(ip);
            log.error("IP {} escalated to blocking due to excessive rate limit violations", ip);
        }
    }

    /**
     * Increment active connection count
     */
    public void incrementActiveConnections() {
        int count = activeConnections.incrementAndGet();
        if (count > 10000) { // Alert threshold
            log.error("ALERT: High number of active connections: {}", count);
        }
    }

    /**
     * Decrement active connection count
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    /**
     * Get current statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalRequests", totalRequests.get(),
            "blockedRequests", blockedRequests.get(),
            "rateLimitedRequests", rateLimitedRequests.get(),
            "activeConnections", activeConnections.get(),
            "suspiciousIpsCount", suspiciousIps.size(),
            "recentBlockedIpsCount", recentBlockedIps.size(),
            "blockRate", calculateBlockRate(),
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    /**
     * Get top suspicious IPs
     */
    public Map<String, Integer> getTopSuspiciousIps(int limit) {
        return suspiciousIps.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().count, e1.getValue().count))
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().count
            ));
    }

    /**
     * Calculate block rate percentage
     */
    private double calculateBlockRate() {
        long total = totalRequests.get();
        if (total == 0) return 0.0;
        return (double) blockedRequests.get() / total * 100.0;
    }

    /**
     * Check if under attack (heuristic)
     */
    public boolean isUnderAttack() {
        // Multiple heuristics to detect attack
        
        // 1. High block rate
        if (calculateBlockRate() > 10.0) {
            return true;
        }
        
        // 2. Too many suspicious IPs
        if (suspiciousIps.size() > 100) {
            return true;
        }
        
        // 3. High rate of blocked requests
        if (blockedRequests.get() > 1000) {
            return true;
        }
        
        return false;
    }

    /**
     * Get attack severity level
     */
    public String getAttackSeverity() {
        if (!isUnderAttack()) {
            return "NORMAL";
        }
        
        double blockRate = calculateBlockRate();
        if (blockRate > 30.0) {
            return "CRITICAL";
        } else if (blockRate > 20.0) {
            return "HIGH";
        } else if (blockRate > 10.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Periodic cleanup of old data
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupOldData() {
        long now = System.currentTimeMillis();
        int[] removedCount = {0};  // Use array to make it effectively final
        
        // Remove old suspicious IPs (older than 1 hour)
        suspiciousIps.entrySet().removeIf(entry -> {
            boolean expired = now - entry.getValue().lastSeen > 3600000;
            if (expired) removedCount[0]++;
            return expired;
        });
        
        // Remove old blocked IPs from tracking (older than 30 minutes)
        recentBlockedIps.entrySet().removeIf(entry -> 
            now - entry.getValue() > 1800000
        );
        
        if (removedCount[0] > 0) {
            log.info("Cleaned up {} old suspicious IP entries", removedCount[0]);
        }
    }

    /**
     * Periodic attack detection and alerting
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void monitorAndAlert() {
        if (isUnderAttack()) {
            String severity = getAttackSeverity();
            log.error("ALERT: Possible DDoS attack detected! Severity: {}", severity);
            log.error("Statistics: {}", getStatistics());
            log.error("Top suspicious IPs: {}", getTopSuspiciousIps(10));
            
            // In production, you would send alerts to monitoring systems here
            // e.g., send email, Slack notification, trigger incident response, etc.
        }
    }

    /**
     * Reset statistics (useful for testing or after attack mitigation)
     */
    public void resetStatistics() {
        totalRequests.set(0);
        blockedRequests.set(0);
        rateLimitedRequests.set(0);
        suspiciousIps.clear();
        recentBlockedIps.clear();
        log.info("DDoS protection statistics reset");
    }

    private static class AttackInfo {
        int count;
        long lastSeen;

        AttackInfo(int count, long lastSeen) {
            this.count = count;
            this.lastSeen = lastSeen;
        }
    }
}

