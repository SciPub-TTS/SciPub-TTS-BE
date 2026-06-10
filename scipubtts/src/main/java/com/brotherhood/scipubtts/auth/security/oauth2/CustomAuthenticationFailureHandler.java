package com.brotherhood.scipubtts.auth.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String errorCode = "oauth2_failed";

        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            String subErrorCode = oAuth2Exception.getError().getErrorCode();

            if ("access_denied".equals(subErrorCode)) {
                errorCode = "cancelled";
            } else if ("invalid_user_info".equals(subErrorCode)
                    || "oauth2_user_not_found".equals(subErrorCode)
                    || "google_email_not_verified".equals(subErrorCode)) {
                errorCode = subErrorCode;
            }
        }

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth2/success")
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
