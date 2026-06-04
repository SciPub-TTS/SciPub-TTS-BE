package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.service.AuthSessionService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

    @Value("${app.frontend.oauth2-success-url}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new ServletException("Invalid OAuth2 principal type. Expected OAuth2User.");
        }

        String email = oauth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"), "Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("oauth2_user_not_found"), "User with email " + email + " not found in system"));

        AuthResponse authResponse = authSessionService.issueSession(
                user,
                false,
                request,
                response);

        String targetUrl = frontendSuccessUrl;
        // User click "Continue with Google"
        // ↓
        // Google redirect về /oauth2/callback (Spring Security tự xử lý)
        // ↓
        // OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess()
        // → authSessionService.issueSession() → set HttpOnly refresh cookie
        // → sendRedirect("http://localhost:5173/oauth2/success") ← No token
        // ↓
        // FE OAuth2SuccessPage mount
        // → POST /api/auth/refresh (browser tự attach cookie)
        // → backend trả accessToken
        // → lưu vào localStorage
        // ↓
        // → GET /api/auth/me
        // → lưu user vào storage
        // ↓
        // → navigate("/")

        authorizationRequestRepository.removeAuthorizationRequest(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
