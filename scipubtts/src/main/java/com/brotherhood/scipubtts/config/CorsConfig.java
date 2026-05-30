package com.brotherhood.scipubtts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration cho FE request từ localhost:5173 tới Backend localhost:8080/api
 *
 * Cấu hình này cho phép:
 * - Preflight OPTIONS request
 * - Cookie + Authorization header (withCredentials: true từ FE)
 * - Credentials mode để refresh token HttpOnly cookie hoạt động
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                // Áp dụng cho tất cả endpoint /api/**
                .addMapping("/api/**")
                // Cho phép request từ FE development + production
                .allowedOrigins(
                        "http://localhost:5173",     // Vite dev server
                        "http://localhost:3000",     // Nếu dùng port khác
                        "https://yourdomain.com"     // Production domain
                )
                // Method cho phép
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Header cho phép trong request
                .allowedHeaders(
                        "Content-Type",
                        "Authorization",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                // Header expose về FE (optional)
                .exposedHeaders(
                        "Authorization",
                        "X-Total-Count",
                        "X-Page-Number"
                )
                // QUAN TRỌNG: Cho phép cookie + Authorization header
                // FE dùng withCredentials: true trong axios, backend phải set true
                .allowCredentials(true)
                // Cache preflight result trong browser (ms)
                .maxAge(30000);

        // Nếu có endpoint khác không dùng /api/** prefix
        registry
                .addMapping("/auth/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://yourdomain.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Nếu có route verify-email redirect từ BE (không standard CORS, chỉ documentation)
        // GET /verify-email?token=... sẽ redirect, không cần CORS setup
    }
}