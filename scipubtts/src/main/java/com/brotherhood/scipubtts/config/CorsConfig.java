package com.brotherhood.scipubtts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://api.kiruaaaa.io.vn/}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Content-Type,Authorization,X-Requested-With,Accept,Origin}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers:Authorization,X-Total-Count,X-Page-Number}")
    private String exposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Tách chuỗi comma-separated từ file properties thành List cấu hình công khai
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        }

        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            config.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        }

        // Credentials — QUAN TRỌNG cho cookie + Authorization
        config.setAllowCredentials(allowCredentials);

        // Cache preflight
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cho tất cả path
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
