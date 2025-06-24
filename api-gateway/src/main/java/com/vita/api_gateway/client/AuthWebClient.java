package com.vita.api_gateway.client;

import com.vita.api_gateway.config.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthWebClient {

    private final WebClient webClient;

    public AuthWebClient(@Value("http://localhost:8082") String authServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public Mono<UserInfo> validateToken(String token) {
        return webClient.get()
                .uri("/auth/user/validate")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(UserInfo.class);
    }
}
