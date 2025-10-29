package com.vita.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthController {

    private final CacheManager cacheManager;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "API Gateway");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Vita API Gateway");
        info.put("version", "1.0.0");
        info.put("description", "Centralized API Gateway for Vita microservices");
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> cacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache instanceof org.springframework.cache.concurrent.ConcurrentMapCache) {
                var nativeCache = (org.springframework.cache.concurrent.ConcurrentMapCache) cache;
                stats.put(cacheName + ".size", nativeCache.getNativeCache().size());
            }
        });
        
        return ResponseEntity.ok(stats);
    }
}
