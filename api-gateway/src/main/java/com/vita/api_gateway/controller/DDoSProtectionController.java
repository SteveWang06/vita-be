package com.vita.api_gateway.controller;

import com.vita.api_gateway.filter.IpBlockingFilter;
import com.vita.api_gateway.service.DDoSProtectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DDoS Protection Management Controller
 * Endpoints for monitoring and managing DDoS protection
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ddos-protection")
@RequiredArgsConstructor
public class DDoSProtectionController {

    private final DDoSProtectionService ddosProtectionService;
    private final IpBlockingFilter ipBlockingFilter;

    /**
     * Get current DDoS protection statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(ddosProtectionService.getStatistics());
    }

    /**
     * Get top suspicious IPs
     */
    @GetMapping("/suspicious-ips")
    public ResponseEntity<Map<String, Integer>> getSuspiciousIps(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ddosProtectionService.getTopSuspiciousIps(limit));
    }

    /**
     * Check if under attack
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean underAttack = ddosProtectionService.isUnderAttack();
        String severity = ddosProtectionService.getAttackSeverity();
        
        return ResponseEntity.ok(Map.of(
            "underAttack", underAttack,
            "severity", severity,
            "statistics", ddosProtectionService.getStatistics()
        ));
    }

    /**
     * Block an IP address
     */
    @PostMapping("/block-ip")
    public ResponseEntity<Map<String, String>> blockIp(@RequestParam String ip) {
        ipBlockingFilter.blockIpTemporarily(ip);
        log.info("IP manually blocked: {}", ip);
        return ResponseEntity.ok(Map.of(
            "message", "IP blocked successfully",
            "ip", ip
        ));
    }

    /**
     * Unblock an IP address
     */
    @PostMapping("/unblock-ip")
    public ResponseEntity<Map<String, String>> unblockIp(@RequestParam String ip) {
        ipBlockingFilter.unblockIp(ip);
        log.info("IP manually unblocked: {}", ip);
        return ResponseEntity.ok(Map.of(
            "message", "IP unblocked successfully",
            "ip", ip
        ));
    }

    /**
     * Check if an IP is blocked
     */
    @GetMapping("/check-ip")
    public ResponseEntity<Map<String, Object>> checkIp(@RequestParam String ip) {
        boolean blocked = ipBlockingFilter.isIpBlocked(ip);
        int violations = ipBlockingFilter.getViolationCount(ip);
        
        return ResponseEntity.ok(Map.of(
            "ip", ip,
            "blocked", blocked,
            "violations", violations
        ));
    }

    /**
     * Reset statistics (use with caution)
     */
    @PostMapping("/reset-statistics")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        ddosProtectionService.resetStatistics();
        log.warn("DDoS protection statistics manually reset");
        return ResponseEntity.ok(Map.of(
            "message", "Statistics reset successfully"
        ));
    }
}

