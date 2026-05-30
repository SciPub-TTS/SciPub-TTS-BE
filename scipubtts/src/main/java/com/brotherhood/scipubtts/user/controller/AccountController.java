package com.brotherhood.scipubtts.user.controller;

import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;
import com.brotherhood.scipubtts.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/change-password")
    public ResponseEntity<ResponseObject> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                         @RequestBody ChangePasswordRequest request) {

        accountService.changePassword(principal.getId(), request);

        return ResponseEntity.ok(
                new ResponseObject(200, "Đổi mật khẩu thành công, vui lòng đăng nhập lại", null)
        );
    }
}
