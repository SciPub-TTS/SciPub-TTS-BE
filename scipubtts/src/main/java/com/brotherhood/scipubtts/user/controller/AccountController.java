package com.brotherhood.scipubtts.user.controller;

import com.brotherhood.scipubtts.auth.dto.response.CurrentUserResponse;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.user.dto.request.ChangePasswordRequest;
import com.brotherhood.scipubtts.user.dto.request.UpdateUserProfileRequest;
import com.brotherhood.scipubtts.user.service.AccountService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/change-password")
    public ResponseEntity<ResponseObject> changePassword(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestBody ChangePasswordRequest request) {

        accountService.changePassword(userId, request);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Password changed successfully, please log in again",
                        null)
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseObject> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        CurrentUserResponse result = accountService.updateProfile(userId, request);
        return ResponseEntity.ok(new ResponseObject(200, "Profile updated successfully", result));
    }
}
