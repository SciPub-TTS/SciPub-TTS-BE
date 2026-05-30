package com.brotherhood.scipubtts.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Optional;

public interface RefreshCookieService {

    public void addRefreshCookie(HttpServletResponse response,
                                 String rawRefreshToken,
                                 boolean rememberMe,
                                 Duration ttl);

    public void clearRefreshCookie(HttpServletResponse response);

    public Optional<String> extractRefreshToken(HttpServletRequest request);
}
