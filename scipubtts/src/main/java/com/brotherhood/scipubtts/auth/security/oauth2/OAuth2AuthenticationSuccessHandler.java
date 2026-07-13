package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.auth.entity.GoogleSignupToken;
import com.brotherhood.scipubtts.auth.repository.GoogleSignupTokenRepository;
import com.brotherhood.scipubtts.auth.dto.response.RefreshTokenResult;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.auth.service.SecureValueService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.user.service.AvatarService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final GoogleSignupTokenRepository googleSignupTokenRepository;
    private final SecureValueService secureValueService;
    private final RefreshTokenService refreshTokenService;
    private final AvatarService avatarService;
    // Team rule:
    // Keep success/failure handlers on the same AuthorizationRequestRepository implementation that
    // SecurityConfig uses for oauth2Login(). If one side uses cookie storage and the other uses
    // HttpSession storage, Google login/register will become flaky on production.
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            authorizationRequestRepository;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new ServletException("Invalid OAuth2 principal type. Expected OAuth2User.");
        }

        String email = oauth2User.getAttribute("email");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");

        if (!StringUtils.hasText(email)) {
            redirectWithError(request, response, "invalid_user_info");
            return;
        }

        var userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.isBanned()) {
                redirectWithError(request, response, "account_banned");
                return;
            }

            user.setGoogleLinked(true);
            user.setEmailVerified(true);

            if (!StringUtils.hasText(user.getFirstName())) {
                user.setFirstName(givenName);
            }

            if (!StringUtils.hasText(user.getLastName())) {
                user.setLastName(familyName);
            }

            if (!StringUtils.hasText(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarService.buildDefaultAvatarUrl(user));
            }

            userRepository.save(user);

            RefreshTokenResult refreshTokenResult = refreshTokenService.issue(
                    user,
                    false,
                    request
            );

            authorizationRequestRepository.removeAuthorizationRequest(request, response);

            String targetUrl = UriComponentsBuilder
                    .fromUriString(frontendBaseUrl)
                    .path("/oauth2/success")
                    .fragment("refreshToken=" + refreshTokenResult.rawToken())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        String rawToken = secureValueService.generateOpaqueToken();

        GoogleSignupToken signupToken = GoogleSignupToken.builder()
                .tokenHash(secureValueService.sha256(rawToken))
                .email(email)
                .firstName(givenName)
                .lastName(familyName)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        googleSignupTokenRepository.save(signupToken);

        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/register/complete")
                .queryParam("token", rawToken)
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
