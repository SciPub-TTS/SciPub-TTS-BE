package com.brotherhood.scipubtts.auth.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    // Team rule:
    // This handler must remove the same OAuth2 authorization request repository used by
    // SecurityConfig and OAuth2AuthenticationSuccessHandler. Keep all 3 in sync.
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {
        String errorCode = "oauth2_failed";

        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            String subErrorCode = oAuth2Exception.getError().getErrorCode();

            if ("access_denied".equals(subErrorCode)) {
                errorCode = "cancelled";
            } else if ("invalid_user_info".equals(subErrorCode)
                    || "oauth2_user_not_found".equals(subErrorCode)
                    || "account_banned".equals(subErrorCode)
                    || "google_email_not_verified".equals(subErrorCode)) {
                errorCode = subErrorCode;
            }
        }

        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth2/success")
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
