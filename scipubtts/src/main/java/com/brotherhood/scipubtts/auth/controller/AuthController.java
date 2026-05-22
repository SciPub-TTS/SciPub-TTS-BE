package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.AuthResponse;
import com.brotherhood.scipubtts.auth.service.AuthService;
import com.brotherhood.scipubtts.auth.dto.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@RequestBody RegisterLocalRequest request, HttpServletRequest httpRequest) {
        String message = authService.registerLocal(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(
                        200,
                        message,
                        null
                )
        );
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token,  HttpServletResponse response) throws IOException {
        String redirectUrl = authService.verifyEmail(token);
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest request) {
        String accessToken = authService.loginLocal(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(
                        200,
                        "Login success",
                        new AuthResponse(accessToken, "Bearer")
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseObject> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(
                        200,
                        "Get Info Success",
                        principal
                )
        );
    }

}
