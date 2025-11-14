package com.vita.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        log.info("╔══════════════════════════════════════════════════════════════");
        log.info("║ INCOMING REQUEST TO API GATEWAY");
        log.info("║ Method: {}", request.getMethod());
        log.info("║ Path: {}", request.getPath().value());
        log.info("║ URI: {}", request.getURI());
        log.info("║ Remote Address: {}", request.getRemoteAddress());
        log.info("║ Headers:");
        request.getHeaders().forEach((name, values) -> {
            if (name.equalsIgnoreCase("Authorization")) {
                log.info("║   {}: Bearer [TOKEN_PRESENT: {}]", name, values.get(0).startsWith("Bearer "));
            } else {
                log.info("║   {}: {}", name, values);
            }
        });
        log.info("╚══════════════════════════════════════════════════════════════");
        
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    log.info("║ RESPONSE Status: {}", exchange.getResponse().getStatusCode());
                    log.info("╚══════════════════════════════════════════════════════════════");
                });
    }

    @Override
    public int getOrder() {
        return -200; // Run before AuthenticationFilter (which has order -100)
    }
}

