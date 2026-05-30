package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.request.LoginRequest;
import com.brotherhood.scipubtts.auth.dto.request.RegisterLocalRequest;
import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    String registerLocal(RegisterLocalRequest request);

    String verifyEmail(String token);

    AuthResponse login(LoginRequest request,
                       HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse);

    AuthResponse refresh(HttpServletRequest request,
                         HttpServletResponse response);

    void logout(UserPrincipal principal,
                HttpServletRequest request,
                HttpServletResponse response);
}
