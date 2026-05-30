package com.brotherhood.scipubtts.auth.controller;

import com.brotherhood.scipubtts.auth.dto.request.ForgotPasswordRequest;
import com.brotherhood.scipubtts.auth.dto.request.ResetPasswordRequest;
import com.brotherhood.scipubtts.auth.dto.request.VerifyResetCodeRequest;
import com.brotherhood.scipubtts.auth.dto.response.VerifyResetCodeResponse;
import com.brotherhood.scipubtts.auth.service.PasswordRecoveryService;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ResponseObject> requestReset(@RequestBody ForgotPasswordRequest request,
                                                       HttpServletRequest httpRequest) {

        passwordRecoveryService.requestReset(request.email(), httpRequest);

        return ResponseEntity.ok(
                new ResponseObject(
                        200,
                        "Nếu email tồn tại trong hệ thống, mã xác thực đã được gửi",
                        null
                )
        );
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ResponseObject> verifyCode(@RequestBody VerifyResetCodeRequest request,
                                                     HttpServletRequest httpRequest) {

        VerifyResetCodeResponse data =
                passwordRecoveryService.verifyCode(request.email(), request.code(), httpRequest);

        return ResponseEntity.ok(
                new ResponseObject(200, "Xác thực mã thành công", data)
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseObject> resetPassword(@RequestBody ResetPasswordRequest request) {

        passwordRecoveryService.resetPassword(
                request.resetGrantToken(),
                request.newPassword(),
                request.confirmNewPassword()
        );

        return ResponseEntity.ok(
                new ResponseObject(200, "Đặt lại mật khẩu thành công, vui lòng đăng nhập lại", null)
        );
    }
}
