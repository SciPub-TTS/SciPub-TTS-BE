package com.brotherhood.scipubtts.admin.controller;

import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.service.AdminService;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<ResponseObject> banUser(
            @Parameter(hidden = true) @CurrentUserUUID UUID adminId,
            @PathVariable UUID userId
    ) {
        AdminUserResponse data = adminService.banUser(adminId, userId);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Account banned successfully",
                        data
                )
        );
    }

    @PatchMapping("/{userId}/unban")
    public ResponseEntity<ResponseObject> unbanUser(
            @Parameter(hidden = true) @CurrentUserUUID UUID adminId,
            @PathVariable UUID userId
    ) {
        AdminUserResponse data = adminService.unbanUser(adminId, userId);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Account unbanned successfully",
                        data
                )
        );
    }
}
