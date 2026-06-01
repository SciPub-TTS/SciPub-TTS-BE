package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.service.AuthSessionService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
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
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Value("${app.frontend.oauth2-success-url:http://localhost:5173/oauth2/success}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String token = jwtTokenService.generateAccessToken(principal);

        AuthResponse authResponse = authSessionService.issueSession(
                user,
                false,
                request,
                response
        );

        String targetUrl = frontendSuccessUrl;

        authorizationRequestRepository.removeAuthorizationRequest(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);


    }
}
