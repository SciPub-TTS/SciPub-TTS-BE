package com.brotherhood.scipubtts.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Optional;

public interface RefreshCookieService {

    void addRefreshCookie(HttpServletResponse response,
                          String rawRefreshToken,
                          boolean rememberMe,
                          Duration ttl);

    void clearRefreshCookie(HttpServletResponse response);

    Optional<String> extractRefreshToken(HttpServletRequest request);
}
