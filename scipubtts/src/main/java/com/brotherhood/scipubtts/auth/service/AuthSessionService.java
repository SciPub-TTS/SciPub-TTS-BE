package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.response.AuthResponse;
import com.brotherhood.scipubtts.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthSessionService {
    AuthResponse issueSession(
            User user,
            boolean rememberMe,
            HttpServletRequest request,
            HttpServletResponse response
    );
}
