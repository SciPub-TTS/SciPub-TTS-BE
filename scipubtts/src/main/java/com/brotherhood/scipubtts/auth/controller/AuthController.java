package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.auth.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.brotherhood.scipubtts.auth.service.AuthService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.user.service.AccountService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AccountService accountService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    public AuthController(
            AuthService authService,
            AccountService accountService,
            HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository
    ) {
        this.authService = authService;
        this.accountService = accountService;
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@Valid @RequestBody RegisterLocalRequest request, HttpServletRequest httpRequest) {
        String message = authService.registerLocal(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        message,
                        null
                )
        );
    }

    @GetMapping("/oauth2/google")
    public void startGoogleAuth(
            @RequestParam(defaultValue = HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_LOGIN) String mode,
            HttpServletResponse response
    ) throws IOException {
        String normalizedMode = mode.trim().toLowerCase();
        if (!HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_LOGIN.equals(normalizedMode)
                && !HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_FLOW_MODE_REGISTER.equals(normalizedMode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Google auth mode");
        }

        authorizationRequestRepository.saveFlowMode(response, normalizedMode);
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
        String redirectUrl = authService.verifyEmail(token);
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {

        AuthResponse data = authService.login(request, httpRequest, httpResponse);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Login successful",
                        data
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject> refresh(HttpServletRequest request,
                                                  HttpServletResponse response) {

        AuthResponse data = authService.refresh(request, response);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Access token refreshed successfully",
                        data
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(principal, request, response);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Logout successful",
                        null
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseObject> me(@Parameter(hidden = true) @CurrentUserUUID UUID userId) {
        CurrentUserResponse data = accountService.getCurrentUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Get user profile successful",
                        data
                )
        );
    }

}
