package com.swp.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> sameSiteCookieFilter() {
        FilterRegistrationBean<OncePerRequestFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                filterChain.doFilter(request, response);
                Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
                boolean firstHeader = true;
                for (String header : headers) {
                    if (firstHeader) {
                        response.setHeader(HttpHeaders.SET_COOKIE, header + "; SameSite=None; Secure");
                        firstHeader = false;
                    } else {
                        response.addHeader(HttpHeaders.SET_COOKIE, header + "; SameSite=None; Secure");
                    }
                }
            }
        });
        filter.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        return filter;
    }
}