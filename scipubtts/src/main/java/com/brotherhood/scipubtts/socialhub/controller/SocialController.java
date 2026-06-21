package com.brotherhood.scipubtts.socialhub.controller;

import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import com.brotherhood.scipubtts.socialhub.service.SocialService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @GetMapping("/newest")
    public ResponseEntity<ResponseObject> getNewest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        UUID viewerId = principal != null ? principal.getId() : null;
        Page<SocialPostSummaryResponse> result = socialService.getNewest(pageable, viewerId);

        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    @GetMapping("/top")
    public ResponseEntity<ResponseObject> getTop(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        UUID viewerId = principal != null ? principal.getId() : null;
        Page<SocialPostSummaryResponse> result = socialService.getTop(pageable, viewerId);

        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ResponseObject> getPostDetail(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID viewerId = principal != null ? principal.getId() : null;
        SocialPostDetailResponse result = socialService.getPostDetail(postId, viewerId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> createPost(
            @Valid @RequestBody CreateSocialPostRequest request,
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        SocialPostDetailResponse data = socialService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Post created", data));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdateSocialPostRequest request,
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        SocialPostDetailResponse data = socialService.updatePost(postId, userId, request);
        return ResponseEntity.ok(new ResponseObject(200, "Post updated", data));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> deletePost(
            @PathVariable UUID postId,
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        socialService.deletePost(postId, userId);
        return ResponseEntity.ok(new ResponseObject(200, "Post deleted", null));
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> toggleLike(
            @PathVariable UUID postId,
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        LikeToggleResponse data = socialService.toggleLike(postId, userId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", data));
    }
}
