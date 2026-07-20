package com.brotherhood.scipubtts.socialhub.service;

import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SocialService {

    SocialPostDetailResponse createPost(UUID authorId, CreateSocialPostRequest request);

    SocialPostPageResponse getNewest(Pageable pageable, UUID viewerId);

    SocialPostPageResponse getTop(Pageable pageable, UUID viewerId);

    SocialPostDetailResponse getPostDetail(UUID postId, UUID viewerId);

    SocialPostDetailResponse updatePost(UUID postId, UUID editorId, UpdateSocialPostRequest request);

    void deletePost(UUID postId, UUID requesterId);

    LikeToggleResponse toggleLike(UUID postId, UUID userId);
}
