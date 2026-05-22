package com.brotherhood.scipubtts.auth.security.oauth2;

import com.brotherhood.scipubtts.user.repository.UserRepository;
import com.brotherhood.scipubtts.auth.security.jwt.JwtTokenService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
            authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Value("${app.frontend.oauth2-success-url:http://localhost:3000/oauth2/success}")
    private String frontendSuccessUrl;

    public OAuth2AuthenticationSuccessHandler(
            JwtTokenService jwtTokenService,
            UserRepository userRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtTokenService.generateAccessToken(principal);
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendSuccessUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        authorizationRequestRepository.removeAuthorizationRequest(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);


    }
}
