package com.brotherhood.scipubtts.socialhub.service;

import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SocialService {

    /** Tạo bài viết mới. Enforce max 3 references trong service. */
    SocialPostDetailResponse createPost(UUID authorId, CreateSocialPostRequest request);

    /** Danh sách feed mới nhất. Hybrid-view: viewerId nullable. */
    Page<SocialPostSummaryResponse> getNewest(Pageable pageable, UUID viewerId);

    /** Danh sách feed hot nhất (like cao). */
    Page<SocialPostSummaryResponse> getTop(Pageable pageable, UUID viewerId);

    /** Cập nhật tiêu đề / nội dung / tag (chỉ tác giả). */
    SocialPostDetailResponse updatePost(UUID postId, UUID editorId, UpdateSocialPostRequest request);

    /** Soft-delete (chỉ tác giả hoặc ADMIN). */
    void deletePost(UUID postId, UUID requesterId);

    /**
     * Toggle like/unlike trong một transaction.
     * - INSERT Social_post_like  + INCREMENT like_count  (nếu chưa like)
     * - DELETE Social_post_like  + DECREMENT like_count  (nếu đã like)
     */
    LikeToggleResponse toggleLike(UUID postId, UUID userId);
}
