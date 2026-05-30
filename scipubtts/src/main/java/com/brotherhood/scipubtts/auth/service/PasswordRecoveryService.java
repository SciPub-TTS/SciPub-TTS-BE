package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.response.VerifyResetCodeResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PasswordRecoveryService {

    void requestReset(String email, HttpServletRequest request);

    VerifyResetCodeResponse verifyCode(String email, String code, HttpServletRequest request);

    void resetPassword(String resetGrantToken, String newPassword, String confirmNewPassword);
}