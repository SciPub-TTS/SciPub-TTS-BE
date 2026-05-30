package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.service.AuthService;
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
    public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
        String redirectUrl = authService.verifyEmail(token);
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {

        AuthResponse data = authService.login(request, httpRequest, httpResponse);

        return ResponseEntity.ok(
                new ResponseObject(200, "Đăng nhập thành công", data)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject> refresh(HttpServletRequest request,
                                                  HttpServletResponse response) {

        AuthResponse data = authService.refresh(request, response);

        return ResponseEntity.ok(
                new ResponseObject(200, "Làm mới access token thành công", data)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(@AuthenticationPrincipal UserPrincipal principal,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {

        authService.logout(principal, request, response);

        return ResponseEntity.ok(
                new ResponseObject(200, "Đăng xuất thành công", null)
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
