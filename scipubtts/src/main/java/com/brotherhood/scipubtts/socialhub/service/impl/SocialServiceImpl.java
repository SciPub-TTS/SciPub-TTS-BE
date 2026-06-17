package com.brotherhood.scipubtts.socialhub.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.PostReferenceRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import com.brotherhood.scipubtts.socialhub.entity.SocialPost;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostLike;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostReference;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostLikeRepository;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostRepository;
import com.brotherhood.scipubtts.socialhub.service.SocialService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private static final int MAX_REFERENCES = 3;
    private static final int BODY_PREVIEW_LENGTH = 200;

    private final SocialPostRepository postRepository;
    private final SocialPostLikeRepository likeRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SocialPostDetailResponse createPost(UUID authorId, CreateSocialPostRequest request) {

        // Rule 1: enforce max 3 references (double-check — annotation @Size đã check,
        //         nhưng ta luôn enforce tại Service như đã thiết kế)
        if (request.references() != null && request.references().size() > MAX_REFERENCES) {
            throw new BusinessException(ErrorCode.Social_POST_TOO_MANY_REFERENCES);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        SocialPost post = new SocialPost();
        post.setAuthor(author);
        post.setTitle(request.title().trim());
        post.setBody(request.body().trim());
        post.setTopicTag(request.topicTag() != null ? request.topicTag().trim() : null);

        // Build references
        if (request.references() != null) {
            List<SocialPostReference> refs = request.references().stream()
                    .map(r -> buildReference(post, r))
                    .collect(Collectors.toList());
            post.setReferences(refs);
        }

        SocialPost saved = postRepository.save(post);
        return toDetailResponse(saved, false);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SocialPostDetailResponse getPost(UUID postId, UUID viewerId) {
        SocialPost post = findActivePost(postId);
        boolean liked = viewerId != null && likeRepository.existsByPostIdAndUserId(postId, viewerId);
        return toDetailResponse(post, liked);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getNewest(Pageable pageable, UUID viewerId) {
        Page<SocialPost> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return toSummaryPage(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getTop(Pageable pageable, UUID viewerId) {
        Page<SocialPost> page = postRepository.findAllByOrderByLikeCountDescCreatedAtDesc(pageable);
        return toSummaryPage(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getByAuthor(UUID authorId, Pageable pageable, UUID viewerId) {
        Page<SocialPost> page = postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
        return toSummaryPage(page, viewerId);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SocialPostDetailResponse updatePost(UUID postId, UUID editorId, UpdateSocialPostRequest request) {
        SocialPost post = findActivePost(postId);
        assertIsAuthor(post, editorId);

        if (StringUtils.hasText(request.title()))    post.setTitle(request.title().trim());
        if (StringUtils.hasText(request.body()))     post.setBody(request.body().trim());
        if (request.topicTag() != null)              post.setTopicTag(request.topicTag().trim());

        SocialPost updated = postRepository.save(post);
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, editorId);
        return toDetailResponse(updated, liked);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE (soft)
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deletePost(UUID postId, UUID requesterId, String requesterRole) {
        SocialPost post = findActivePost(postId);

        boolean isAdmin  = "ROLE_ADMIN".equals(requesterRole);
        boolean isAuthor = post.getAuthor().getId().equals(requesterId);

        if (!isAdmin && !isAuthor) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        post.softDelete();
        postRepository.save(post);
    }

    // ─────────────────────────────────────────────────────────
    // LIKE TOGGLE — đây là điểm quan trọng nhất:
    // INSERT like + INCREMENT like_count nằm chung 1 @Transactional
    // dùng UPDATE ... SET like_count = like_count + 1 tránh race condition
    // ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LikeToggleResponse toggleLike(UUID postId, UUID userId) {
        SocialPost post = findActivePost(postId);

        Optional<SocialPostLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            // UNLIKE
            likeRepository.delete(existing.get());
            postRepository.decrementLikeCount(postId);

            // Đọc lại số like mới nhất (sau decrement)
            int newCount = findActivePost(postId).getLikeCount();
            return new LikeToggleResponse(false, newCount);

        } else {
            // LIKE
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            SocialPostLike like = new SocialPostLike();
            like.setPost(post);
            like.setUser(user);
            likeRepository.save(like);

            postRepository.incrementLikeCount(postId);

            int newCount = findActivePost(postId).getLikeCount();
            return new LikeToggleResponse(true, newCount);
        }
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    private SocialPost findActivePost(UUID postId) {
        // @SQLRestriction tự filter deleted_at IS NULL
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.Social_POST_NOT_FOUND));
    }

    private void assertIsAuthor(SocialPost post, UUID userId) {
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * Chuẩn hóa OpenAlex ID — đồng bộ với module Bookmark.
     * Input:  "https://openalex.org/W123456789" hoặc "W123456789" hoặc "w123456789"
     * Output: "W123456789"
     */
    private String normalizeOpenAlexId(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        // Bóc tách URL nếu có
        if (trimmed.contains("/")) {
            trimmed = trimmed.substring(trimmed.lastIndexOf('/') + 1);
        }
        return trimmed.toUpperCase();
    }

    private SocialPostReference buildReference(SocialPost post, PostReferenceRequest req) {
        SocialPostReference ref = new SocialPostReference();
        ref.setPost(post);
        ref.setOpenalexId(normalizeOpenAlexId(req.openalexId()));
        ref.setTitleSnapshot(req.titleSnapshot());
        ref.setAuthorsSnapshot(req.authorsSnapshot());
        ref.setSourceSnapshot(req.sourceSnapshot());
        ref.setYearSnapshot(req.yearSnapshot() != null ? req.yearSnapshot().shortValue() : null);
        ref.setDoiSnapshot(req.doiSnapshot());
        return ref;
    }

    /**
     * Hybrid-view: batch check liked status cho toàn bộ page một lần query
     * thay vì N+1 queries.
     */
    private Page<SocialPostSummaryResponse> toSummaryPage(Page<SocialPost> page, UUID viewerId) {
        List<UUID> postIds = page.stream().map(SocialPost::getId).toList();

        Set<UUID> likedIds = (viewerId != null && !postIds.isEmpty())
                ? likeRepository.findLikedPostIds(viewerId, postIds)
                : Set.of();

        return page.map(post -> toSummaryResponse(post, likedIds.contains(post.getId())));
    }

    private SocialPostSummaryResponse toSummaryResponse(SocialPost post, boolean liked) {
        String preview = post.getBody().length() > BODY_PREVIEW_LENGTH
                ? post.getBody().substring(0, BODY_PREVIEW_LENGTH) + "…"
                : post.getBody();

        User author = post.getAuthor();
        String fullName = (author.getFirstName() + " " + author.getLastName()).trim();

        return new SocialPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                preview,
                post.getTopicTag(),
                post.getLikeCount(),
                liked,
                new SocialPostSummaryResponse.AuthorInfo(author.getId(), fullName),
                post.getCreatedAt()
        );
    }

    private SocialPostDetailResponse toDetailResponse(SocialPost post, boolean liked) {
        User author = post.getAuthor();
        String fullName = (author.getFirstName() + " " + author.getLastName()).trim();

        List<SocialPostDetailResponse.ReferenceInfo> refs = post.getReferences().stream()
                .map(r -> new SocialPostDetailResponse.ReferenceInfo(
                        r.getId(),
                        r.getOpenalexId(),
                        r.getTitleSnapshot(),
                        r.getAuthorsSnapshot(),
                        r.getSourceSnapshot(),
                        r.getYearSnapshot() != null ? r.getYearSnapshot().intValue() : null,
                        r.getDoiSnapshot()
                ))
                .toList();

        return new SocialPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getTopicTag(),
                post.getLikeCount(),
                liked,
                new SocialPostDetailResponse.AuthorInfo(author.getId(), fullName),
                refs,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
