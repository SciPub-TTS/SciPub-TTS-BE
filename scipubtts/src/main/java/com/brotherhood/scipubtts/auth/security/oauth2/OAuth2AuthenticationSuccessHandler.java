package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.auth.entity.GoogleSignupToken;
import com.brotherhood.scipubtts.auth.repository.GoogleSignupTokenRepository;
import com.brotherhood.scipubtts.auth.service.AuthSessionService;
import com.brotherhood.scipubtts.auth.service.SecureValueService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthSessionService authSessionService;
    private final UserRepository userRepository;
    private final GoogleSignupTokenRepository googleSignupTokenRepository;
    private final SecureValueService secureValueService;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
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

        /*
         * CASE 1:
         * Email Google đã tồn tại trong DB
         * => Login bằng Google luôn.
         */
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.isBanned()) {
                redirectWithError(request, response, "account_banned");
                return;
            }

            /*
             * User local trước đó đăng ký email/password,
             * giờ bấm Google login thì link Google vào account.
             */
            user.setGoogleLinked(true);

            /*
             * Vì Google trả email_verified = true ở CustomOAuth2UserService rồi,
             * nên có thể xem email này đã verified.
             */
            user.setEmailVerified(true);

            if (!StringUtils.hasText(user.getFirstName())) {
                user.setFirstName(givenName);
            }

            if (!StringUtils.hasText(user.getLastName())) {
                user.setLastName(familyName);
            }

            userRepository.save(user);

            authSessionService.issueSession(
                    user,
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
            return;
        }

        /*
         * CASE 2:
         * Email Google chưa tồn tại trong DB
         * => Không tạo user ngay.
         * => Tạo google signup token.
         * => Redirect FE sang trang nhập password.
         */
        String rawToken = secureValueService.generateOpaqueToken();

        GoogleSignupToken signupToken = GoogleSignupToken.builder()
                .tokenHash(secureValueService.sha256(rawToken))
                .email(email)
                .firstName(givenName)
                .lastName(familyName)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        googleSignupTokenRepository.save(signupToken);
        try {
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
                .path("/register/complete")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
            String targetUrl = UriComponentsBuilder
                    .fromUriString(frontendBaseUrl)
                    .path("/oauth2/success")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception ex) {
            log.error("Google OAuth2 success flow failed after callback", ex);
            redirectWithError(request, response, "oauth2_failed");
        }
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