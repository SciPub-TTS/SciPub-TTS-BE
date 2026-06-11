package com.brotherhood.scipubtts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            config.setAllowedOrigins(splitAndTrimCsv(allowedOrigins));
        }

        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            config.setAllowedMethods(splitAndTrimCsv(allowedMethods));
        }

        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            config.setAllowedHeaders(splitAndTrimCsv(allowedHeaders));
        }

        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            config.setExposedHeaders(splitAndTrimCsv(exposedHeaders));
        }

        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private List<String> splitAndTrimCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }
}
