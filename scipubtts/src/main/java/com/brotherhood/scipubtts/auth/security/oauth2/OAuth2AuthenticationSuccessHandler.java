package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.auth.service.AuthSessionService;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AuthSessionService authSessionService;
    private final UserRepository userRepository;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new ServletException("Invalid OAuth2 principal type. Expected OAuth2User.");
        }

        String email = oauth2User.getAttribute("email");
        if (email == null) {
            redirectWithError(request, response, "invalid_user_info");
            return;
        }

        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            redirectWithError(request, response, "oauth2_user_not_found");
            return;
        }

        authSessionService.issueSession(
                userOptional.get(),
                false,
                request,
                response
        );

        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth2/success")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void redirectWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorCode
    ) throws IOException {
        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth2/success")
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
