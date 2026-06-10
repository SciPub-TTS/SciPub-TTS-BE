package com.brotherhood.scipubtts.follow.controller;

import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.follow.dto.request.CreateFollowRequest;
import com.brotherhood.scipubtts.follow.dto.response.FollowPageResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowResponse;
import com.brotherhood.scipubtts.follow.dto.response.FollowStatusResponse;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // ==========================================
    // 1. CREATE FOLLOW
    // ==========================================
    @PostMapping
    public ResponseEntity<ResponseObject> followTarget(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @Valid @RequestBody CreateFollowRequest request) {

        FollowResponse data = followService.followTarget(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Follow saved successfully", data));
    }

    // ==========================================
    // 2. UNFOLLOW TARGET
    // ==========================================
    @DeleteMapping
    public ResponseEntity<ResponseObject> unfollowTarget(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam @NotNull(message = "Target type is required") FollowTargetType targetType,
            @RequestParam @NotBlank(message = "Target OpenAlex ID is required") String targetOpenAlexId) {

        followService.unfollowTarget(userId, targetType, targetOpenAlexId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Unfollowed successfully", null)
        );
    }

    // ==========================================
    // 3. GET FOLLOW LIST (LAZY PAGING)
    // ==========================================
    @GetMapping
    public ResponseEntity<ResponseObject> getMyFollows(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) FollowTargetType targetType,
            @RequestParam(defaultValue = "RECENT") String sort) {

        FollowPageResponse data = followService.getMyFollows(
                userId, page, size, keyword, targetType, sort
        );

        return ResponseEntity.ok(
                new ResponseObject(200, "Follows fetched successfully", data)
        );
    }

    // ==========================================
    // 4. CHECK FOLLOW STATUS
    // ==========================================
    @GetMapping("/status")
    public ResponseEntity<ResponseObject> getFollowStatus(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam @NotNull(message = "Target type query parameter is required") FollowTargetType targetType,
            @RequestParam @NotBlank(message = "Target OpenAlex ID query parameter is required") String targetOpenAlexId) {

        FollowStatusResponse data = followService.getFollowStatus(userId, targetType, targetOpenAlexId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Follow status checked successfully", data)
        );
    }
}
