package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.request.CompleteGoogleRegisterRequest;
import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.OAuth2SessionExchangeRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.auth.dto.response.GoogleSignupPreviewResponse;
import com.brotherhood.scipubtts.auth.service.AuthService;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.user.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
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

    public AuthController(
            AuthService authService,
            AccountService accountService
    ) {
        this.authService = authService;
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a local account",
            description = "Creates a new local account and sends an email verification link."
    )
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
    public void startGoogleAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email",
            description = "Opens the email verification flow with the token sent to the user's email."
    )
    public void verifyEmail(
            @Parameter(
                    description = "Email verification token from the verification email.",
                    example = "eyJhbGciOiJIUzI1NiJ9.verify.email.token"
            )
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl = authService.verifyEmail(token);
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticates the user and returns access token data. A refresh token cookie is also issued."
    )
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
    @Operation(
            summary = "Refresh access token",
            description = "Uses the refresh token cookie issued at login. No request body is required."
    )
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

    @PostMapping("/oauth2/exchange")
    public ResponseEntity<ResponseObject> exchangeOAuth2Session(
            @Valid @RequestBody OAuth2SessionExchangeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse data = authService.exchangeOAuth2Session(
                request,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "OAuth2 session created successfully",
                        data
                )
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Revokes the current session and clears the refresh token cookie."
    )
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
    @Operation(
            summary = "Get current user profile",
            description = "Returns profile information for the currently authenticated user."
    )
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

    @PostMapping("/register/google/complete")
    public ResponseEntity<ResponseObject> completeGoogleRegister(
            @Valid @RequestBody CompleteGoogleRegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse data = authService.completeGoogleRegister(request, httpRequest, httpResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(
                        HttpStatus.CREATED.value(),
                        "Register with Google successfully",
                        data
                )
        );
    }

    @GetMapping("/register/google/preview")
    public ResponseEntity<ResponseObject> previewGoogleRegister(@RequestParam String token) {
        GoogleSignupPreviewResponse data = authService.previewGoogleRegister(token);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Get Google signup information successfully",
                        data
                )
        );
    }
}
