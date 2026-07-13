package com.brotherhood.scipubtts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String refreshCookieName = "refresh_token";
    private String refreshCookiePath = "/api/auth";
    private boolean refreshCookieSecure = false;
    private String refreshCookieSameSite = "Lax";
    private long refreshTokenHours = 24;
    private long rememberMeRefreshTokenDays = 30;

    private int passwordResetCodeLength = 6;
    private long passwordResetCodeMinutes = 10;
    private long passwordResetGrantMinutes = 10;
    private int passwordResetMaxAttempts = 5;

    private boolean exposeSecurityErrorDetails = false;

    public Duration refreshTokenTtl() {
        return Duration.ofHours(refreshTokenHours);
    }

    public Duration rememberMeRefreshTokenTtl() {
        return Duration.ofDays(rememberMeRefreshTokenDays);
    }

    public Duration passwordResetCodeTtl() {
        return Duration.ofMinutes(passwordResetCodeMinutes);
    }

    public Duration passwordResetGrantTtl() {
        return Duration.ofMinutes(passwordResetGrantMinutes);
    }
}
