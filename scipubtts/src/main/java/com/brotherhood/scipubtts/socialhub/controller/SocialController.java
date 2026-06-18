package com.brotherhood.scipubtts.socialhub.controller;

import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import com.brotherhood.scipubtts.socialhub.service.SocialService;
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

    // ─────────────────────────────────────────────────────────
    // PUBLIC — khách vãng lai được đọc, hybrid-view khi đăng nhập
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/Socials/newest?page=0&size=10
     * Public. Trả về liked=true nếu user đã xác thực và đã like.
     */
    @GetMapping("/newest")
    public ResponseEntity<ResponseObject> getNewest(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal   // null nếu không authen
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        UUID viewerId = principal != null ? principal.getId() : null;
        Page<SocialPostSummaryResponse> result = socialService.getNewest(pageable, viewerId);

        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    /**
     * GET /api/Socials/top?page=0&size=10
     * Public. Sắp xếp theo like_count DESC.
     */
    @GetMapping("/top")
    public ResponseEntity<ResponseObject> getTop(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        UUID viewerId = principal != null ? principal.getId() : null;
        Page<SocialPostSummaryResponse> result = socialService.getTop(pageable, viewerId);

        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    /**
     * GET /api/Socials/{postId}
     * Public. Trả về chi tiết bao gồm references.
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ResponseObject> getPost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID viewerId = principal != null ? principal.getId() : null;
        SocialPostDetailResponse data = socialService.getPost(postId, viewerId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", data));
    }

    /**
     * GET /api/Socials/author/{authorId}?page=0&size=10
     * Public. Lấy bài viết của một tác giả.
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<ResponseObject> getByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        UUID viewerId = principal != null ? principal.getId() : null;
        Page<SocialPostSummaryResponse> result = socialService.getByAuthor(authorId, pageable, viewerId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
    }

    // ─────────────────────────────────────────────────────────
    // AUTHENTICATED — yêu cầu đăng nhập
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/Socials
     * Tạo bài viết mới. Body gồm title, body, topicTag, references (≤3).
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> createPost(
            @Valid @RequestBody CreateSocialPostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SocialPostDetailResponse data = socialService.createPost(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Post created", data));
    }

    /**
     * PUT /api/Socials/{postId}
     * Cập nhật title / body / topicTag. Chỉ tác giả.
     */
    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdateSocialPostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SocialPostDetailResponse data = socialService.updatePost(postId, principal.getId(), request);
        return ResponseEntity.ok(new ResponseObject(200, "Post updated", data));
    }

    /**
     * DELETE /api/Socials/{postId}
     * Soft-delete. Tác giả hoặc ADMIN.
     */
    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String role = principal.getAuthorities().iterator().next().getAuthority();
        socialService.deletePost(postId, principal.getId(), role);
        return ResponseEntity.ok(new ResponseObject(200, "Post deleted", null));
    }

    /**
     * POST /api/Socials/{postId}/like
     * Toggle like/unlike. Trả về trạng thái mới và like_count.
     */
    @PostMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> toggleLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        LikeToggleResponse data = socialService.toggleLike(postId, principal.getId());
        return ResponseEntity.ok(new ResponseObject(200, "Success", data));
    }

}
