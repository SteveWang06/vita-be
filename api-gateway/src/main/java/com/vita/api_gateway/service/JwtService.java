package com.vita.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.signerKey:1TjXchw5FloESb63Kc+DFhTARvpWL4jUGCwfGWxuG5SIf/1y/LgJxHnMqaF6A/ij}")
    private String signerKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(signerKey.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(signerKey.getBytes())).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenNotExpired(String token) {
        try {
            String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(pureToken)
                    .getPayload();

            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            log.debug("JWT expiration check failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(pureToken)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            log.debug("Failed to extract username from JWT: {}", e.getMessage());
            return null;
        }
    }
}
