package com.vita.api_gateway.service;

import com.vita.api_gateway.config.UserInfo;
import com.vita.api_gateway.client.AuthWebClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final AuthWebClient authWebClient;
    private final JwtService jwtService;

    @Cacheable(value = "token-cache", key = "#token")
    @CircuitBreaker(name = "auth-service", fallbackMethod = "validateTokenFallback")
    @RateLimiter(name = "auth-service")
    public Mono<UserInfo> validateToken(String token) {
        if (!jwtService.isValidJwtFormat(token)) {
            return Mono.error(new IllegalArgumentException("Invalid JWT format"));
        }

        return authWebClient.validateToken(token)
                .map(response -> {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(response.getUserId());
                    userInfo.setUsername(response.getUsername());
                    userInfo.setRole(response.getRole());
                    return userInfo;
                });
    }

    private Mono<UserInfo> validateTokenFallback(String token, Throwable ex) {
        log.warn("Auth service unavailable, using fallback validation for token: {}", token);
        
        if (jwtService.isTokenNotExpired(token)) {
            String username = jwtService.extractUsername(token);
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(username);
            return Mono.just(userInfo);
        }
        
        return Mono.error(new IllegalArgumentException("Token validation failed"));
    }
}
