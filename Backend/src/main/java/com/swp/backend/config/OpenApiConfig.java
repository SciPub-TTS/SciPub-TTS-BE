package com.swp.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.public-base-url}") String publicBaseUrl) {
        Server server = new Server();
        server.setUrl(publicBaseUrl);

        return new OpenAPI().servers(List.of(server));
    }
}
