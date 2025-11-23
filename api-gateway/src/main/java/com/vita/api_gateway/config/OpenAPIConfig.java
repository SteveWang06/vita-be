package com.vita.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Vita Backend API Gateway")
                .description("Centralized API Gateway for all Vita Backend microservices. Routes requests to: Auth Service, User Service, Post Service, Rental Service, Media Service")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Vita Backend Team")
                    .email("support@vita.com")
                    .url("https://vita.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("API Gateway - All API calls should go through this gateway")
            ));
    }
}

