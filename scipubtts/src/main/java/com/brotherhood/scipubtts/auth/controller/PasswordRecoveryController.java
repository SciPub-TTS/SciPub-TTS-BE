package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.request.ForgotPasswordRequest;
import com.brotherhood.scipubtts.auth.dto.request.ResetPasswordRequest;
import com.brotherhood.scipubtts.auth.dto.request.VerifyResetCodeRequest;
import com.brotherhood.scipubtts.auth.dto.response.VerifyResetCodeResponse;
import com.brotherhood.scipubtts.auth.service.PasswordRecoveryService;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/request")
    public ResponseEntity<ResponseObject> requestReset(@Valid @RequestBody ForgotPasswordRequest request,
                                                       HttpServletRequest httpRequest) {

        passwordRecoveryService.requestReset(request.email(), httpRequest);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "If the email exists in our system, a verification code has been sent",
                        null
                )
        );
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ResponseObject> verifyCode(@Valid @RequestBody VerifyResetCodeRequest request,
                                                     HttpServletRequest httpRequest) {

        VerifyResetCodeResponse data =
                passwordRecoveryService.verifyCode(request.email(), request.code(), httpRequest);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Verification code verified successfully",
                        data
                )
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseObject> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        passwordRecoveryService.resetPassword(
                request.resetGrantToken(),
                request.newPassword(),
                request.confirmNewPassword()
        );

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Password reset successful, please log in again",
                        null
                )
        );
    }
}
